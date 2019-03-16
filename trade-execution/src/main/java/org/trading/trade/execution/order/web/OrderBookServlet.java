package org.trading.trade.execution.order.web;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import net.minidev.json.JSONObject;
import org.trading.api.event.LimitOrderAccepted;
import org.trading.api.event.TradeExecuted;
import org.trading.health.HealthCheckServer;
import org.trading.messaging.Message;
import org.trading.messaging.netty.TcpDataSource;
import org.trading.trade.execution.order.domain.OrderBook;
import org.trading.trade.execution.order.domain.OrderLevelListener;
import org.trading.trade.execution.order.event.LastTradeExecuted;
import org.trading.trade.execution.order.event.OrderLevelUpdated;
import org.trading.trade.execution.order.web.json.LastTradeToJson;
import org.trading.trade.execution.order.web.json.OrderLevelToJson;
import org.trading.web.SseEventDispatcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import static com.lmax.disruptor.dsl.ProducerType.SINGLE;
import static com.lmax.disruptor.util.DaemonThreadFactory.INSTANCE;
import static java.lang.Integer.parseInt;
import static org.trading.messaging.Message.EventType.EventTypeVisitor;
import static org.trading.messaging.Message.EventType.SUBSCRIBE;
import static org.trading.messaging.Message.FACTORY;

public class OrderBookServlet extends HttpServlet implements EventHandler<Message> {
    private final SseEventDispatcher eventDispatcher = new SseEventDispatcher();
    private final OrderLevelToJson orderLevelToJson = new OrderLevelToJson();
    private final LastTradeToJson lastTradeToJson = new LastTradeToJson();
    private final Map<String, JSONObject> lastTrades = new ConcurrentHashMap<>();
    private final HealthCheckServer healthCheckServer;

    private final OrderBook orderBook = new OrderBook(new OrderLevelListener() {
        @Override
        public void onOrderLevelUpdated(OrderLevelUpdated orderLevelUpdated) {
            JSONObject json = orderLevelToJson.toJson(orderLevelUpdated);
            eventDispatcher.dispatchEvent("orderLevelUpdated", json.toJSONString());
        }

        @Override
        public void onLastTradeExecuted(LastTradeExecuted lastTradeExecuted) {
            JSONObject json = lastTradeToJson.toJson(lastTradeExecuted);
            eventDispatcher.dispatchEvent("lastTradeUpdated", json.toJSONString());

            lastTrades.merge(lastTradeExecuted.symbol, json, (value1, value2) -> {
                        json.replace("lastQuantity", value1.getAsNumber("lastQuantity").intValue() + value2.getAsNumber("lastQuantity").intValue());
                        return json;
                    }

            );

        }
    });

    private Disruptor<Message> disruptor;

    public OrderBookServlet(HealthCheckServer healthCheckServer) {
        this.healthCheckServer = healthCheckServer;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String host = config.getInitParameter("host");
        int port = parseInt(config.getInitParameter("port"));
        disruptor = new Disruptor<>(FACTORY, 1024, INSTANCE, SINGLE, new BlockingWaitStrategy());
        disruptor.handleEventsWith(this);
        disruptor.start();
        TcpDataSource dataSource = new TcpDataSource(
                host,
                port,
                disruptor,
                "Orderbook",
                healthCheckServer
        );
        dataSource.connect();
        new Timer().schedule(new TimerTask() {
            public void run() {
                orderBook.updateIndicators();
                lastTrades.forEach((symbol, json) -> {
                    json.replace("time", LocalDateTime.now().toString());
                    eventDispatcher.dispatchEvent("indicatorsUpdated", json.toJSONString());
                });
                lastTrades.clear();
            }
        }, 1000L, 1000L * 2L);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // String symbol = req.getPathInfo().split("/")[1];
        eventDispatcher.register(req, resp);
        disruptor.publishEvent((event, sequence) -> event.type = SUBSCRIBE);
    }

    @Override
    public void onEvent(Message message, long sequence, boolean endOfBatch) {
        message.type.accept(new EventTypeVisitor<Void>() {

            @Override
            public Void visitSubmitOrder() {
                return null;
            }

            @Override
            public Void visitSubscribe() {
                orderBook.subscribe();
                return null;
            }

            @Override
            public Void visitMarketOrderAccepted() {
                return null;
            }

            @Override
            public Void visitLimitOrderAccepted() {
                final LimitOrderAccepted limitOrderAccepted = (LimitOrderAccepted) message.event;
                orderBook.onLimitOrderPlaced(limitOrderAccepted);
                return null;
            }

            @Override
            public Void visitMarketOrderRejected() {
                return null;
            }

            @Override
            public Void visitTradeExecuted() {
                final TradeExecuted tradeExecuted = (TradeExecuted) message.event;
                orderBook.onTradeExecuted(tradeExecuted);
                return null;
            }

            @Override
            public Void visitRequestExecution() {
                return null;
            }

            @Override
            public Void visitUpdateQuantities() {
                return null;
            }

            @Override
            public Void visitOrderBookCreated() {
                return null;
            }
        });
    }
}
