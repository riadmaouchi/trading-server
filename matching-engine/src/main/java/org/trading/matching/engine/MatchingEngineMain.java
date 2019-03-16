package org.trading.matching.engine;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.dsl.Disruptor;
import org.trading.api.message.SubmitOrder;
import org.trading.discovery.RemoteProviderFactory;
import org.trading.discovery.Service;
import org.trading.serviceregistry.ServiceRegistry;
import org.trading.eventstore.domain.DomainEvent;
import org.trading.eventstore.store.EventDispatcher;
import org.trading.eventstore.store.EventStoreCache;
import org.trading.eventstore.store.IDRepositoryCache;
import org.trading.eventstore.store.ldb.LdbEventStore;
import org.trading.eventstore.store.ldb.LdbRepository;
import org.trading.health.HealthCheckServer;
import org.trading.matching.engine.domain.MatchingEngine;
import org.trading.matching.engine.domain.OrderBook;
import org.trading.matching.engine.domain.OrderBook.OrderDomainEvent;
import org.trading.matching.engine.domain.OrderBookRepository;
import org.trading.matching.engine.eventstore.BuyLimitOrderFullyExecutedSerializer;
import org.trading.matching.engine.eventstore.BuyLimitOrderPlacedSerializer;
import org.trading.matching.engine.eventstore.LimitOrderAcceptedSerializer;
import org.trading.matching.engine.eventstore.LimitOrderQuantityFilledSerializer;
import org.trading.matching.engine.eventstore.MarketOrderAcceptedSerializer;
import org.trading.matching.engine.eventstore.MarketOrderQuantityFilledSerializer;
import org.trading.matching.engine.eventstore.MarketOrderRejectedSerializer;
import org.trading.matching.engine.eventstore.OrderBookCreatedSerializer;
import org.trading.matching.engine.eventstore.SellLimitOrderFullyExecutedSerializer;
import org.trading.matching.engine.eventstore.SellLimitOrderPlacedSerializer;
import org.trading.matching.engine.eventstore.TradeExecutedSerializer;
import org.trading.matching.engine.view.ViewRepository;
import org.trading.matching.engine.view.ViewStore;
import org.trading.messaging.Message;
import org.trading.messaging.Message.EventType.EventTypeVisitor;
import org.trading.messaging.netty.TcpDataSource;

import java.util.List;
import java.util.concurrent.Executors;

import static com.lmax.disruptor.dsl.ProducerType.MULTI;
import static com.lmax.disruptor.dsl.ProducerType.SINGLE;
import static com.lmax.disruptor.util.DaemonThreadFactory.INSTANCE;
import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;
import static java.time.Clock.systemUTC;
import static java.util.Optional.ofNullable;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.CONSUL;
import static org.trading.discovery.RemoteProviderFactory.RemoteProvider.DEFAULT;
import static org.trading.discovery.RemoteProviderFactory.getFactory;

public final class MatchingEngineMain {

    public static void main(String[] args) {
        new MatchingEngineMain().start();
    }

    private void start() {

        String version = ofNullable(getClass().getPackage().getImplementationVersion())
                .orElse("undefined");
        String name = ofNullable(getClass().getPackage().getImplementationTitle())
                .orElse("undefined");

       /* new HTTPServer(1234);

        Counter requests = Counter.build()
                .name("java_app_requests_total")
                .help("Total requests.")
                .register();*/

        String host = ofNullable(getenv("HOSTNAME")).orElse("localhost");
        String dbPath = ofNullable(getenv("DB_PATH")).orElse("./target");

        int httpMonitoringPort = parseInt(ofNullable(getenv("HTTP_MONITORING_PORT")).orElse("9997"));

        RemoteProviderFactory.RemoteProvider provider = ofNullable(getenv("CONSUL_URL"))
                .map(s -> CONSUL).orElse(DEFAULT);
        HealthCheckServer healthCheckServer = new HealthCheckServer(host, httpMonitoringPort, version, name);
        healthCheckServer.start();

        ServiceRegistry serviceRegistry = getFactory(provider, healthCheckServer).getServiceConfiguration();

        Disruptor<Message> outboundDisruptor = new Disruptor<>(
                Message.FACTORY,
                1024,
                INSTANCE,
                SINGLE,
                new BlockingWaitStrategy()
        );

        outboundDisruptor.start();

        int servicePort = parseInt(ofNullable(getenv("TCP_PORT")).orElse("8980"));

        serviceRegistry.register(new Service(
                "matchingengine",
                servicePort,
                3L,
                "matchingengine",
                httpMonitoringPort,
                "tcp"
        ), host, "localhost");


        ViewRepository viewRepository = new ViewRepository(dbPath + "/orderviews");
        final ViewStore store = new ViewStore(viewRepository);
        RingBuffer<Message> ringBuffer = outboundDisruptor.getRingBuffer();
        BatchEventProcessor<Message> processor = new BatchEventProcessor<>(ringBuffer, ringBuffer.newBarrier(), store);
        Sequence sequence1 = processor.getSequence();
        ringBuffer.addGatingSequences(sequence1);
        Executors.newSingleThreadExecutor().execute(processor);

        serviceRegistry.discover(outboundDisruptor, viewRepository::loadAll, "order", "pricer");

        final ExchangeResponsePublisher orderEventListener = new ExchangeResponsePublisher(outboundDisruptor);
        final LdbEventStore<DomainEvent> eventstore = new LdbEventStore<>(dbPath + "/eventstore", List.of(
                new OrderBookCreatedSerializer(),
                new LimitOrderAcceptedSerializer(),
                new MarketOrderAcceptedSerializer(),
                new SellLimitOrderPlacedSerializer(),
                new BuyLimitOrderPlacedSerializer(),
                new TradeExecutedSerializer(),
                new MarketOrderRejectedSerializer(),
                new MarketOrderQuantityFilledSerializer(),
                new LimitOrderQuantityFilledSerializer(),
                new SellLimitOrderFullyExecutedSerializer(),
                new BuyLimitOrderFullyExecutedSerializer()
        ));
        final OrderBookRepository repository = new OrderBookRepository(() -> new OrderBook(systemUTC()));
        MatchingEngine matchingEngine = new MatchingEngine(
                new IDRepositoryCache<>(new LdbRepository(dbPath + "/orderbookByID")),
                new EventStoreCache<>(
                        new EventDispatcher<OrderDomainEvent>(event -> event.accept(orderEventListener)),
                        eventstore,
                        repository
                )
        );

        Disruptor<Message> inboundDisruptor = new Disruptor<>(
                Message.FACTORY,
                1024,
                INSTANCE,
                MULTI,
                new BlockingWaitStrategy()
        );

        inboundDisruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            // requests.inc();
            event.type.accept(new EventTypeVisitor<Void>() {
                @Override
                public Void visitSubmitOrder() {
                    matchingEngine.submitOrder((SubmitOrder) event.event);
                    return null;
                }

                @Override
                public Void visitSubscribe() {
                    return null;
                }

                @Override
                public Void visitMarketOrderAccepted() {
                    return null;
                }

                @Override
                public Void visitLimitOrderAccepted() {
                    return null;
                }

                @Override
                public Void visitMarketOrderRejected() {
                    return null;
                }

                @Override
                public Void visitTradeExecuted() {
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
                    matchingEngine.orderBookConfig((String) event.event);
                    return null;
                }
            });

        });
        inboundDisruptor.start();

        serviceRegistry.update(values -> values.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("symbol"))
                .filter(entry -> entry.getKey().endsWith("price"))
                .forEach(value -> {
                    inboundDisruptor.publishEvent((event, sequence) -> {
                        event.type = Message.EventType.ORDER_BOOK_CREATED;
                        event.event = value.getKey().split("/")[1];
                    });
                })
        );


        new TcpDataSource(host, 8980, inboundDisruptor, "matchingengine", healthCheckServer)
                .connect();

    }
}
