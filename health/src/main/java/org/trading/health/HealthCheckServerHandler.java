package org.trading.health;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Scanner;
import java.util.SortedMap;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpUtil.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspHeaderNames.CONTENT_LENGTH;
import static java.util.stream.Collectors.joining;
import static org.slf4j.LoggerFactory.getLogger;

public class HealthCheckServerHandler extends ChannelInboundHandlerAdapter {
    private final static Logger LOGGER = getLogger(HealthCheckServerHandler.class);

    private final String badge;
    private final byte[] favicon;
    private final byte[] jquery;
    private final byte[] bootstrapJS;
    private final byte[] bootstrapCSS;
    private final String index;
    private final String healthcheck;
    private final HealthCheckRegistry healthCheckRegistry;

    HealthCheckServerHandler(String version, String name, HealthCheckRegistry healthCheckRegistry) {
        this.healthCheckRegistry = healthCheckRegistry;
        this.healthCheckRegistry.register("Thread Deadlock", new ThreadDeadlockHealthCheck());
        badge = loadFileFromClassPath("static/badge.svg").replace("VERSION", version);
        favicon = loadFileFromClassPath("static/favicon.ico").getBytes();
        jquery = loadFileFromClassPath("static/jquery-3.3.1.min.js").getBytes();
        bootstrapJS = loadFileFromClassPath("static/bootstrap/js/bootstrap.min.js").getBytes();
        bootstrapCSS = loadFileFromClassPath("static/bootstrap/css/bootstrap.min.css").getBytes();
        index = loadFileFromClassPath("static/index.html")
                .replace("APP_NAME", name)
                .replace("VERSION", version);
        healthcheck = loadFileFromClassPath("static/healthcheck.html");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            if (is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }

            if ("/badge".equals(req.uri())) {
                SortedMap<String, HealthCheck.Result> checks = healthCheckRegistry.runHealthChecks();
                boolean isHealthy = checks.values().stream().allMatch(HealthCheck.Result::isHealthy);

                String badge = this.badge
                        .replace("STATUS", isHealthy ? "ok" : "ko")
                        .replace("COLOR", isHealthy ? "#4c1" : "#e05d44");

                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, wrappedBuffer(badge.getBytes()));
                response.headers().set(CONTENT_TYPE, "image/svg+xml");
                sendHttpResponse(ctx, req, response);
            } else if ("/static/bootstrap.min.css".equals(req.uri())) {
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, wrappedBuffer(bootstrapCSS));
                response.headers().set(CONTENT_TYPE, "text/css");
                sendHttpResponse(ctx, req, response);
            } else if ("/static/jquery-3.3.1.min.js".equals(req.uri())) {
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, wrappedBuffer(jquery));
                response.headers().set(CONTENT_TYPE, "application/javascript");
                sendHttpResponse(ctx, req, response);
            } else if ("/static/bootstrap.min.js".equals(req.uri())) {
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, wrappedBuffer(bootstrapJS));
                response.headers().set(CONTENT_TYPE, "application/javascript");
                sendHttpResponse(ctx, req, response);
            } else if ("/favicon.ico".equals(req.uri())) {
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, wrappedBuffer(favicon));
                response.headers().set(CONTENT_TYPE, "image/x-icon");
                sendHttpResponse(ctx, req, response);
            } else {
                SortedMap<String, HealthCheck.Result> checks = healthCheckRegistry.runHealthChecks();
                boolean isHealthy = checks.values().stream().allMatch(HealthCheck.Result::isHealthy);

                String healthchecks = checks.entrySet().stream().map(entry -> healthcheck
                        .replace("NAME", entry.getKey())
                        .replace("REASON", Optional.ofNullable(entry.getValue().getMessage()).orElse(""))
                        .replace("STYLE", entry.getValue().isHealthy() ? "success" : "danger")
                        .replace("STATUS", entry.getValue().isHealthy() ? "Healthy" : "UnHealthy"))
                        .collect(joining());

                String index = this.index
                        .replace("OVERALL_STATUS", isHealthy ? "Healthy" : "UnHealthy")
                        .replace("OVERALL_COLOR", isHealthy ? "success" : "danger")
                        .replace("HEALTHCHECK", healthchecks);
                HttpResponseStatus statusCode = isHealthy ? OK : INTERNAL_SERVER_ERROR;
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, statusCode, wrappedBuffer(index.getBytes()));

                response.headers().set(CONTENT_TYPE, "text/html");
                sendHttpResponse(ctx, req, response);
            }
        }
    }

    private String loadFileFromClassPath(String path) {
        return new Scanner(getClass().getClassLoader().getResourceAsStream(path)).useDelimiter("\\A").next();
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, FullHttpResponse response) {
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

        if (isKeepAlive(req)) {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        } else {
            ctx.write(response).addListener(CLOSE);
        }
    }


    public void register(String name, HealthCheck healthCheck) {
        healthCheckRegistry.register(name, healthCheck);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error(cause.getMessage());
        ctx.close();
    }
}