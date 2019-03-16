package org.trading.health;

import com.codahale.metrics.health.HealthCheckRegistry;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

public class HealthCheckInitializer extends ChannelInitializer<SocketChannel> {

    private final String version;
    private final String name;
    private final HealthCheckRegistry healthCheckRegistry;

    HealthCheckInitializer(String version, String name, HealthCheckRegistry healthCheckRegistry) {
        this.version = version;
        this.name = name;
        this.healthCheckRegistry = healthCheckRegistry;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HealthCheckServerHandler(version, name, healthCheckRegistry));
    }
}
