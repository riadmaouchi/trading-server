package org.trading.market;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import net.minidev.json.JSONObject;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trading.api.command.OrderType.OrderTypeVisitor;
import org.trading.api.command.Side.SideVisitor;
import org.trading.configuration.Configuration;
import org.trading.discovery.RemoteProviderFactory;
import org.trading.discovery.Service;
import org.trading.discovery.ServiceConfiguration;
import org.trading.health.HealthCheckServer;
import org.trading.market.command.*;
import org.trading.market.command.Command.EventType.EventTypeVisitor;
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
import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static java.util.Optional.ofNullable;
import static org.eclipse.jetty.http.HttpMethod.POST;
import static org.slf4j.LoggerFactory.getLogger;
import static org.trading.configuration.Configuration.create;
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

        String host = getProperty("docker.container.id", "localhost");
        String consulEnabled = getProperty("consul.enabled", "false");

        String version = ofNullable(getClass().getPackage().getImplementationVersion())
                .orElse("undefined");
        String name = ofNullable(getClass().getPackage().getImplementationTitle())
                .orElse("undefined");

        RemoteProviderFactory.RemoteProvider provider = parseBoolean(consulEnabled) ? CONSUL : DEFAULT;
        ServiceConfiguration serviceConfiguration = getFactory(provider).getServiceConfiguration();

        HttpClient httpClient = new HttpClient();
        httpClient.start();

        Configuration configuration = create();

        int httpMonitoringPort = configuration.getInt("monitoring.port");

        new HealthCheckServer(host, httpMonitoringPort, version, name).start();

        Service service = new Service(
                "market",
                httpMonitoringPort,
                3L,
                "market",
                httpMonitoringPort,
                "http"
        );

        serviceConfiguration.register(service, host);

        serviceConfiguration.discover("order");

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
            serviceConfiguration.get("order").ifPresent(url -> httpClient.newRequest(url)
                    .path("/v1/order/new")
                    .method(POST)
                    .content(new StringContentProvider(submitOrder.toJSONString()))
                    .send(result -> LOGGER.info("Request complete {} ",result)));
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

        serviceConfiguration.update(values -> values.entrySet().stream()
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

        subscribe(serviceConfiguration, inboundDisruptor);


        final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        new RandomExecutor(scheduledExecutorService, () -> inboundDisruptor.publishEvent((event, sequence) -> {
            event.type = SUBMIT_LIMIT_ORDER;
            event.event = new SubmitLimitOrder();
        }), 1000);
    }

    private static void subscribe(ServiceConfiguration serviceConfiguration, Disruptor<Command> inboundDisruptor) {
        serviceConfiguration.get("order").ifPresentOrElse(url -> new EventSource(url, "/v1/book/EURUSD")
                        .addEventListener(new EventHandler("lastTradeUpdated", jsonObject -> {
                            inboundDisruptor.publishEvent((event, sequence) -> {
                                event.type = LAST_TRADE_PRICE;
                                event.event = new LastTradePrice(
                                        jsonObject.getAsString("symbol"),
                                        jsonObject.getAsNumber("lastPrice").doubleValue()
                                );
                            });
                        })),
                () -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    subscribe(serviceConfiguration, inboundDisruptor);
                });
    }

    private static class RandomExecutor implements Runnable {
        private static final Random rand = new Random();
        private ScheduledExecutorService ses;
        private Runnable runnable;
        private int maxSleep;

        RandomExecutor(ScheduledExecutorService ses, Runnable runnable, int maxSleep) {
            this.ses = ses;
            this.runnable = runnable;
            this.maxSleep = maxSleep;
            ses.execute(this);
        }

        @Override
        public void run() {
            runnable.run();
            ses.schedule(this, rand.nextInt(maxSleep) + 1, TimeUnit.MILLISECONDS);
        }
    }
}
