package org.trading.messaging.netty;

import com.lmax.disruptor.dsl.Disruptor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.trading.messaging.Message;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.trading.health.HealthCheckServer.register;

@ChannelHandler.Sharable
public class TcpDataSource extends ChannelInboundHandlerAdapter {
    private final ServerBootstrap bootStrap = new ServerBootstrap();
    private final ExecutorService executorService = newSingleThreadExecutor();
    private final InetSocketAddress socketAddress;
    private ChannelHandlerContext ctx;

    public TcpDataSource(String host,
                         int port,
                         Disruptor<Message> disruptor,
                         String id) {
        this(new InetSocketAddress(host, port), disruptor, id);
    }

    private TcpDataSource(InetSocketAddress socketAddress,
                          Disruptor<Message> disruptor,
                          String id) {
        this.socketAddress = socketAddress;
        register(id + " data source", new HealthCheck());

        EventLoopGroup serverGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        bootStrap.group(serverGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new ProtobufVarint32FrameDecoder());
                        p.addLast(new ProtobufDecoder(org.trading.Message.getDefaultInstance()));
                        p.addLast(new ProtobufVarint32LengthFieldPrepender());
                        p.addLast(new MessageHandler(disruptor));
                        p.addLast(TcpDataSource.this);
                    }
                });
    }

    public void connect() {
        executorService.submit(() -> {
            try {
                bootStrap.bind(socketAddress).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }

    private class HealthCheck extends com.codahale.metrics.health.HealthCheck {

        @Override
        protected Result check() {
            if (ctx != null && ctx.channel().isActive()) {
                String hostAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
                return healthy("Listening on %s : [peers : %s]", socketAddress, hostAddress);
            }
            return healthy("No connection(s). (%s)", socketAddress);

        }
    }
}
