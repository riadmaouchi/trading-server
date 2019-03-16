package org.trading.messaging.netty;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.trading.MessageProvider;
import org.trading.MessageProvider.EventType;
import org.trading.MessageProvider.LimitOrderAccepted;
import org.trading.MessageProvider.MarketOrderAccepted;
import org.trading.MessageProvider.MarketOrderRejected;
import org.trading.MessageProvider.SubmitOrder;
import org.trading.MessageProvider.TradeExecuted;
import org.trading.health.HealthCheckServer;
import org.trading.messaging.Message;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static com.codahale.metrics.health.HealthCheck.Result.unhealthy;
import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static io.netty.channel.ChannelOption.SO_REUSEADDR;
import static io.netty.channel.ChannelOption.TCP_NODELAY;
import static org.slf4j.LoggerFactory.getLogger;

@ChannelHandler.Sharable
public class TcpEventHandler extends ChannelInboundHandlerAdapter implements EventHandler<Message>, LifecycleAware {
    private static final Logger LOGGER = getLogger(TcpEventHandler.class);
    private final Bootstrap bootstrap = new Bootstrap();
    private final Timer timer = new Timer();
    private final InetSocketAddress socketAddress;
    private final Supplier<List<MessageProvider.Message>> supplier;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private Channel channel;
    private AtomicBoolean connected = new AtomicBoolean(false);
    private ChannelHandlerContext ctx;
    private final List<MessageProvider.Message> pendingMessages = new ArrayList<>();

    public TcpEventHandler(String host,
                           int port,
                           String id,
                           Supplier<List<MessageProvider.Message>> supplier,
                           HealthCheckServer healthCheckServer) {
        this(new InetSocketAddress(host, port), id, supplier, healthCheckServer);

    }

    private TcpEventHandler(InetSocketAddress socketAddress,
                            String id,
                            Supplier<List<MessageProvider.Message>> supplier,
                            HealthCheckServer healthCheckServer) {
        this.socketAddress = socketAddress;
        LOGGER.error("Socket Address {}", socketAddress);
        this.supplier = supplier;
        healthCheckServer.register(id + " publisher", new HealthCheck());
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .option(SO_KEEPALIVE, true)
                .option(TCP_NODELAY, true)
                .option(SO_REUSEADDR, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new LoggingHandler(LogLevel.INFO));
                        p.addLast(new ProtobufVarint32LengthFieldPrepender());
                        p.addLast(new ProtobufEncoder());
                        p.addLast(TcpEventHandler.this);
                    }
                });
    }

    public void awaitShutdown() throws InterruptedException {
        shutdownLatch.await();
    }

    @Override
    public void onShutdown() {
        shutdownLatch.countDown();
        if (channel != null)
            close();
    }

    @Override
    public void onStart() {
        scheduleConnect(1000);
    }


    @Override
    public void onEvent(Message event, long sequence, boolean endOfBatch) {
        MessageProvider.Message.Builder messageBuilder = MessageProvider.Message.newBuilder();

        event.type.accept(new Message.EventType.EventTypeVisitor<Void>() {
            @Override
            public Void visitSubmitOrder() {
                messageBuilder.setEvenType(EventType.SUBMIT_ORDER);
                messageBuilder.setSubmitOrder((SubmitOrder) event.event);
                return null;
            }

            @Override
            public Void visitSubscribe() {
                return null;
            }

            @Override
            public Void visitMarketOrderAccepted() {
                messageBuilder.setEvenType(EventType.MARKET_ORDER_ACCEPTED);
                messageBuilder.setMarketOrderAccepted((MarketOrderAccepted) event.event);
                return null;
            }

            @Override
            public Void visitLimitOrderAccepted() {
                messageBuilder.setEvenType(EventType.LIMIT_ORDER_ACCEPTED);
                messageBuilder.setLimitOrderAccepted((LimitOrderAccepted) event.event);
                return null;
            }

            @Override
            public Void visitMarketOrderRejected() {
                messageBuilder.setEvenType(EventType.MARKET_ORDER_REJECTED);
                messageBuilder.setMarketOrderRejected((MarketOrderRejected) event.event);
                return null;
            }

            @Override
            public Void visitTradeExecuted() {
                messageBuilder.setEvenType(EventType.TRADE_EXECUTED);
                messageBuilder.setTradeExecuted((TradeExecuted) event.event);
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
        pendingMessages.add(messageBuilder.build());

        if (ctx != null && endOfBatch) {
            pendingMessages.forEach(message -> ctx.writeAndFlush(message));
            pendingMessages.clear();
        }
    }


    public void close() {
        try {
            channel.close().sync();
            connected.set(false);
        } catch (InterruptedException e) {
            LOGGER.error("fail to close", e);
            Thread.currentThread().interrupt();
        }
    }

    private void connect() {
        try {
            ChannelFuture f = bootstrap.connect(socketAddress);
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (!future.isSuccess()) {
                        future.channel().close();
                        bootstrap.connect(socketAddress).addListener(this);
                    } else {
                        channel = future.channel();
                        channel.closeFuture().addListener(f -> {
                            connected.set(false);
                            LOGGER.info("connection Lost");
                            scheduleConnect(5);
                        });
                        connected.set(true);
                        LOGGER.info("connection established");
                    }
                }

            });
        } catch (
                Exception ex) {
            scheduleConnect(1000);
        }
    }

    private void scheduleConnect(long millis) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                connect();
            }
        }, millis);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
        LOGGER.info("channelActive " + ctx);
        List<MessageProvider.Message> messageList = supplier.get();
        LOGGER.info("connection established then sending " + ctx);
        messageList.forEach(ctx::writeAndFlush);
        pendingMessages.forEach(ctx::writeAndFlush);
        pendingMessages.clear();
    }

    private class HealthCheck extends com.codahale.metrics.health.HealthCheck {

        @Override
        protected Result check() {

            if (channel.isActive()) {
                return healthy("Remote address is %s", channel.remoteAddress());
            }
            return unhealthy("Fail to connect on %s", socketAddress);
        }
    }
}
