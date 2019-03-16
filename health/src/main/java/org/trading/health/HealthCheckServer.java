package org.trading.health;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;

import java.net.InetSocketAddress;

import static io.netty.channel.ChannelOption.SO_BACKLOG;
import static io.netty.handler.logging.LogLevel.INFO;
import static org.slf4j.LoggerFactory.getLogger;

public final class HealthCheckServer {
    private final static Logger LOGGER = getLogger(HealthCheckServer.class);
    private final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
    private Channel channel;
    private final InetSocketAddress inetSocketAddress;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final String version;
    private final String name;

    public HealthCheckServer(String host, int port, String version, String name) {
        this.version = version;
        this.name = name;
        inetSocketAddress = new InetSocketAddress(host, port);
    }

    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(INFO))
                    .childHandler(new HealthCheckInitializer(version, name, healthCheckRegistry));

            channel = b.bind(inetSocketAddress).sync().channel();
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted!", e);
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        if (channel == null) {
            return;
        }
        try {
            channel.close().sync();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted!", e);
            Thread.currentThread().interrupt();
        }

    }

    public void register(String name, HealthCheck healthCheck) {
        healthCheckRegistry.register(name, healthCheck);
    }
}
