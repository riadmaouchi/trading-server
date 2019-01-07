package org.trading.messaging.netty;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.trading.*;
import org.trading.messaging.Message;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static com.codahale.metrics.health.HealthCheck.Result.unhealthy;
import static io.netty.channel.ChannelOption.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.trading.health.HealthCheckServer.register;

@ChannelHandler.Sharable
public class TcpEventHandler extends ChannelInboundHandlerAdapter implements EventHandler<Message>, LifecycleAware {
    private static final Logger LOGGER = getLogger(TcpEventHandler.class);
    private final Bootstrap bootstrap = new Bootstrap();
    private final Timer timer = new Timer();
    private final InetSocketAddress socketAddress;
    protected final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private Channel channel;
    private AtomicBoolean connected = new AtomicBoolean(false);
    private ChannelHandlerContext ctx;

    public TcpEventHandler(String host,
                           int port,
                           String id) {
        this(new InetSocketAddress(host, port), id);

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

    private TcpEventHandler(InetSocketAddress socketAddress, String id) {
        this.socketAddress = socketAddress;
        register(id + " publisher", new HealthCheck());
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

    @Override
    public void onEvent(Message event, long sequence, boolean endOfBatch) {
        org.trading.Message.Builder messageBuilder = org.trading.Message.newBuilder();

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
            public Void visitMarketOrderPlaced() {
                messageBuilder.setEvenType(EventType.MARKET_ORDER_PLACED);
                messageBuilder.setMarketOrderPlaced((MarketOrderPlaced) event.event);
                return null;
            }

            @Override
            public Void visitLimitOrderPlaced() {
                messageBuilder.setEvenType(EventType.LIMIT_ORDER_PLACED);
                messageBuilder.setLimitOrderPlaced((LimitOrderPlaced) event.event);
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
        });

        if (ctx != null) {
            ctx.writeAndFlush(messageBuilder.build());
        }
    }


    public void close() {
        try {
            channel.close().sync();
            connected.set(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        } catch (Exception ex) {
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
