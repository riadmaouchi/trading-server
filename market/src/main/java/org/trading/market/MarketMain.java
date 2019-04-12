package org.trading.market;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import net.minidev.json.JSONObject;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.slf4j.Logger;
import org.trading.api.message.OrderType.OrderTypeVisitor;
import org.trading.api.message.Side.SideVisitor;
import org.trading.discovery.RemoteProviderFactory;
import org.trading.discovery.Service;
import org.trading.serviceregistry.ServiceRegistry;
import org.trading.health.HealthCheckServer;
import org.trading.market.command.Command;
import org.trading.market.command.Command.EventType.EventTypeVisitor;
import org.trading.market.command.LastTradePrice;
import org.trading.market.command.SubmitLimitOrder;
import org.trading.market.command.UpdateCurrencies;
import org.trading.market.command.UpdatePrecision;
import org.trading.market.domain.CommandListener;
import org.trading.market.domain.MarketService;
import org.trading.market.event.Event;
import org.trading.market.event.OrderSubmitted;
import org.trading.web.EventSource;
import org.trading.web.EventSource.EventHandler;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.lmax.disruptor.dsl.ProducerType.MULTI;
import static com.lmax.disruptor.dsl.ProducerType.SINGLE;
import static com.lmax.disruptor.util.DaemonThreadFactory.INSTANCE;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;
import static org.eclipse.jetty.http.HttpMethod.POST;
import static org.slf4j.LoggerFactory.getLogger;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.CONSUL;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.DEFAULT;
import static org.trading.discovery.RemoteProviderFactory.getFactory;
import static org.trading.market.command.Command.EventType.LAST_TRADE_PRICE;
import static org.trading.market.command.Command.EventType.SUBMIT_LIMIT_ORDER;
import static org.trading.market.command.Command.FACTORY;

public final class MarketMain {

    private static final Logger LOGGER = getLogger(MarketMain.class);

    private MarketMain() {
    }

    public static void main(String[] args) throws Exception {
        new MarketMain().start();
    }

    private void start() throws Exception {

        String host = ofNullable(getenv("HOSTNAME")).orElse("localhost");

        String version = ofNullable(getClass().getPackage().getImplementationVersion())
                .orElse("undefined");
        String name = ofNullable(getClass().getPackage().getImplementationTitle())
                .orElse("undefined");

        String getenv = getenv("CONSUL_URL");

        int httpMonitoringPort = parseInt(ofNullable(getenv("HTTP.MONITORING.PORT")).orElse("9996"));

        HealthCheckServer healthCheckServer = new HealthCheckServer(host, httpMonitoringPort, version, name);
        healthCheckServer.start();

        RemoteProviderFactory.RemoteProvider provider = ofNullable(getenv)
                .map(s -> CONSUL).orElse(DEFAULT);
        ServiceRegistry serviceRegistry = getFactory(provider, healthCheckServer, getenv).getServiceConfiguration();

        HttpClient httpClient = new HttpClient();
        httpClient.start();




        Service service = new Service(
                "market",
                httpMonitoringPort,
                3L,
                "market",
                httpMonitoringPort,
                "http"
        );

        serviceRegistry.register(service, host, "localhost");

        serviceRegistry.discover("order");

        Disruptor<Event> outboundDisruptor = new Disruptor<>(
                Event.FACTORY,
                1024,
                INSTANCE,
                SINGLE,
                new BlockingWaitStrategy()
        );

        outboundDisruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            OrderSubmitted orderSubmitted = (OrderSubmitted) event.event;
            final JSONObject submitOrder = new JSONObject();
            submitOrder.put("symbol", orderSubmitted.symbol);
            submitOrder.put("broker", orderSubmitted.broker);
            submitOrder.put("amount", orderSubmitted.amount);
            submitOrder.put("side", orderSubmitted.side.accept(new SideVisitor<String>() {
                @Override
                public String visitBuy() {
                    return "buy";
                }

                @Override
                public String visitSell() {
                    return "sell";
                }
            }));
            submitOrder.put("type", orderSubmitted.type.accept(new OrderTypeVisitor<String>() {
                @Override
                public String visitMarket() {
                    return "market";
                }

                @Override
                public String visitLimit() {
                    return "limit";
                }
            }));
            submitOrder.put("price", orderSubmitted.price);
            serviceRegistry.get("order").ifPresent(url -> httpClient.newRequest(url)
                    .path("/v1/order/new")
                    .method(POST)
                    .content(new StringContentProvider(submitOrder.toJSONString()))
                    .send(result -> LOGGER.info("Request complete {} ", result)));
        });
        outboundDisruptor.start();

        CommandListener marketService = new MarketService(orderSubmitted -> outboundDisruptor.publishEvent((event, sequence) -> {
            event.type = Event.EventType.ORDER_SUBMITTED;
            event.event = orderSubmitted;
        }));


        Disruptor<Command> inboundDisruptor = new Disruptor<>(
                FACTORY,
                1024,
                INSTANCE,
                MULTI,
                new BlockingWaitStrategy()
        );

        inboundDisruptor.handleEventsWith((event, sequence, endOfBatch) -> event.type.accept(new EventTypeVisitor<Void>() {

            @Override
            public Void visitSubmitLimitOrder() {
                marketService.onSubmitLimitOrder((SubmitLimitOrder) event.event);
                return null;
            }

            @Override
            public Void visitLastTradePrice() {
                marketService.onLastTradePrice((LastTradePrice) event.event);
                return null;
            }

            @Override
            public Void visitUpdatePrecision() {
                marketService.updatePrecision((UpdatePrecision) event.event);
                return null;
            }

            @Override
            public Void visitUpdateCurrencies() {
                marketService.updateCurrencies((UpdateCurrencies) event.event);
                return null;
            }
        }));
        inboundDisruptor.start();

        serviceRegistry.update(values -> values.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("symbol"))
                .forEach(value -> {
                    String[] strings = value.getKey().split("/");
                    if (strings[2].equals("precision")) {
                        value.getValue().ifPresent(value1 ->
                                inboundDisruptor.publishEvent((event, sequence) -> {
                                    event.type = Command.EventType.UPDATE_PRECISION;
                                    event.event = new UpdatePrecision(strings[1], parseInt(value1));
                                })
                        );
                    } else if (strings[2].equals("price")) {
                        value.getValue().ifPresent(value1 -> inboundDisruptor.publishEvent((event, sequence) -> {
                            event.type = Command.EventType.UPDATE_CURRENCIES;
                            event.event = new UpdateCurrencies(strings[1], parseDouble(value1));
                        }));
                    }
                })
        );

        subscribe(serviceRegistry, inboundDisruptor);


        final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        new RandomExecutor(scheduledExecutorService, () -> inboundDisruptor.publishEvent((event, sequence) -> {
            event.type = SUBMIT_LIMIT_ORDER;
            event.event = new SubmitLimitOrder();
        }), 200L);
    }

    private static void subscribe(ServiceRegistry serviceRegistry, Disruptor<Command> inboundDisruptor) {
        serviceRegistry.get("order").ifPresentOrElse(url -> new EventSource(url, "/v1/book/EURUSD")
                        .addEventListener(new EventHandler("lastTradeUpdated", jsonObject -> {
                            inboundDisruptor.publishEvent((event, sequence) -> {
                                event.type = LAST_TRADE_PRICE;
                                event.event = new LastTradePrice(
                                        jsonObject.getAsString("symbol"),
                                        jsonObject.getAsNumber("lastPrice").doubleValue()
                                );
                            });
                        })),
                () -> subscribe(serviceRegistry, inboundDisruptor));
    }

    private static class RandomExecutor implements Runnable {
        private static final Random random = new Random();
        private ScheduledExecutorService ses;
        private Runnable runnable;
        private long maxSleep;


        RandomExecutor(ScheduledExecutorService ses, Runnable runnable, long maxSleep) {
            this.ses = ses;
            this.runnable = runnable;
            this.maxSleep = maxSleep;
            ses.execute(this);
        }

        @Override
        public void run() {
            runnable.run();
            ses.schedule(this, 1 + (long) (Math.random() * (maxSleep - 1)), TimeUnit.MILLISECONDS);
        }
    }
}
