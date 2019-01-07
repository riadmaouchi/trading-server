package org.trading.pricing.web;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.api.event.TradeExecuted;
import org.trading.discovery.ServiceConfiguration;
import org.trading.messaging.Message;
import org.trading.messaging.netty.TcpDataSource;
import org.trading.pricing.domain.PricingService;
import org.trading.pricing.web.json.PricesToJson;
import org.trading.web.SseEventDispatcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.lmax.disruptor.dsl.ProducerType.SINGLE;
import static com.lmax.disruptor.util.DaemonThreadFactory.INSTANCE;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.trading.messaging.Message.EventType.SUBSCRIBE;
import static org.trading.messaging.Message.EventType.UPDATE_QUANTITIES;
import static org.trading.messaging.Message.FACTORY;

public class PricingServlet extends HttpServlet implements EventHandler<Message> {
    private final SseEventDispatcher eventDispatcher = new SseEventDispatcher();
    private final PricesToJson pricesToJson = new PricesToJson();
    private final PricingService pricingService;
    private final ServiceConfiguration serviceConfiguration;
    private Disruptor<Message> disruptor;

    public PricingServlet(IntArrayList quantities, ServiceConfiguration serviceConfiguration) {
        pricingService = new PricingService(prices -> {
            String json = pricesToJson.toJson(prices).toJSONString();
            eventDispatcher.dispatchEvent("price", json);
        }, quantities);
        this.serviceConfiguration = serviceConfiguration;
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
                "Pricing"
        );
        dataSource.connect();
        serviceConfiguration.update(values -> values.entrySet().stream()
                .filter(value -> value.getKey().equals("ladder/quantities"))
                .findAny().ifPresent(value -> value.getValue().ifPresent(v -> {
                    var quantities = stream(v.split(","))
                            .map(String::trim)
                            .mapToInt(Integer::parseInt)
                            .boxed()
                            .collect(toList());
                    dispatchEvent(quantities);
                }))
        );
    }

    private void dispatchEvent(List<Integer> quantities) {
        disruptor.publishEvent((event, sequence) -> {
            event.type = UPDATE_QUANTITIES;
            event.event = quantities;
        });
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        eventDispatcher.register(req, resp);
        disruptor.publishEvent((event, sequence) -> event.type = SUBSCRIBE);
    }

    @Override
    public void onEvent(Message message, long sequence, boolean endOfBatch) {
        Message.EventType eventType = message.type;
        switch (eventType) {
            case SUBSCRIBE:
                pricingService.getLastPrices().forEach(price -> eventDispatcher.dispatchEvent(
                        "price",
                        pricesToJson.toJson(price).toJSONString())
                );
                break;
            case LIMIT_ORDER_PLACED:
                LimitOrderPlaced limitOrderPlaced = (LimitOrderPlaced) message.event;
                pricingService.onLimitOrderPlaced(limitOrderPlaced);
                break;
            case TRADE_EXECUTED:
                TradeExecuted tradeExecuted = (TradeExecuted) message.event;
                pricingService.onTradeExecuted(tradeExecuted);
                break;
            case UPDATE_QUANTITIES:
                List<Integer> quantities = (List<Integer>) message.event;
                pricingService.updateQuantities(quantities);
                break;
            default:
                break;
        }

    }

}
