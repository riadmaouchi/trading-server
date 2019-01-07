package org.trading.trade.execution.order.web;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.api.event.MarketOrderPlaced;
import org.trading.api.event.TradeExecuted;
import org.trading.messaging.Message;
import org.trading.messaging.netty.TcpDataSource;
import org.trading.trade.execution.order.domain.Blotter;
import org.trading.trade.execution.order.web.json.OrderUpdatedToJson;
import org.trading.web.SseEventDispatcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.lmax.disruptor.dsl.ProducerType.SINGLE;
import static com.lmax.disruptor.util.DaemonThreadFactory.INSTANCE;
import static java.lang.Integer.parseInt;
import static org.trading.messaging.Message.EventType.SUBSCRIBE;
import static org.trading.messaging.Message.FACTORY;

public class BlotterServlet extends HttpServlet implements EventHandler<Message> {
    private Disruptor<Message> disruptor;
    private final SseEventDispatcher eventDispatcher = new SseEventDispatcher();
    private final OrderUpdatedToJson orderUpdatedToJson = new OrderUpdatedToJson();
    private final Blotter blotter = new Blotter(orderUpdated -> {
        String json = orderUpdatedToJson.toJson(orderUpdated).toJSONString();
        eventDispatcher.dispatchEvent("orderEvent", json);
    });

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String host = config.getInitParameter("host");
        int port = parseInt(config.getInitParameter("port"));
        disruptor = new Disruptor<>(FACTORY, 1024, INSTANCE, SINGLE, new BlockingWaitStrategy());

        TcpDataSource tcpDataSource = new TcpDataSource(
                host,
                port,
                disruptor,
                "Blotter"
        );
        tcpDataSource.connect();

        disruptor.handleEventsWith(this);
        disruptor.start();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // String id = req.getPathInfo().split("/")[1];
        eventDispatcher.register(req, resp);
        disruptor.publishEvent((message, sequence) -> {
            //  message.event = id;
            message.type = SUBSCRIBE;
        });
    }

    @Override
    public void onEvent(Message message, long sequence, boolean endOfBatch) {
        final Message.EventType eventType = message.type;
        switch (eventType) {
            case LIMIT_ORDER_PLACED:
                final LimitOrderPlaced limitOrderPlaced = (LimitOrderPlaced) message.event;
                blotter.onLimitOrderPlaced(limitOrderPlaced);
                break;
            case MARKET_ORDER_PLACED:
                final MarketOrderPlaced marketOrderPlaced = (MarketOrderPlaced) message.event;
                blotter.onMarketOrderPlaced(marketOrderPlaced);
                break;
            case TRADE_EXECUTED:
                final TradeExecuted tradeExecuted = (TradeExecuted) message.event;
                blotter.onTradeExecuted(tradeExecuted);
                break;
            case SUBSCRIBE:
                // final String id = ((String) message.event);
                blotter.onSubscribe();
                break;
            default:
                break;
        }
    }
}
