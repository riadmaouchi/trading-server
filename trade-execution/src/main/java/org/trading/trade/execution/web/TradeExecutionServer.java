package org.trading.trade.execution.web;

import com.codahale.metrics.servlets.PingServlet;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.trading.discovery.ServiceConfiguration;
import org.trading.health.HttpHealthCheck;
import org.trading.messaging.Message;
import org.trading.trade.execution.esp.web.ExecutionServlet;
import org.trading.trade.execution.order.web.BlotterServlet;
import org.trading.trade.execution.order.web.OrderBookServlet;
import org.trading.trade.execution.order.web.OrderServlet;

import static com.lmax.disruptor.dsl.ProducerType.MULTI;
import static com.lmax.disruptor.util.DaemonThreadFactory.INSTANCE;
import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;
import static org.trading.health.HealthCheckServer.register;
import static org.trading.messaging.Message.FACTORY;

;

public class TradeExecutionServer {

    private final ServerConfiguration configuration;
    private final ServiceConfiguration serviceConfiguration;
    private final String host;
    private Server server;

    public TradeExecutionServer(ServerConfiguration configuration,
                                ServiceConfiguration serviceConfiguration,
                                String host) {
        this.configuration = configuration;
        this.serviceConfiguration = serviceConfiguration;
        this.host = host;
    }

    public void start() throws Exception {
        server = new Server(configuration.tradeServerPort());
        ServletContextHandler context = new ServletContextHandler(NO_SESSIONS);
        context.setContextPath("/");
        context.addFilter(new FilterHolder(new CrossOriginFilter()), "/*", null);

        final ServletHolder pingServlet = new ServletHolder(new PingServlet());
        context.addServlet(pingServlet, "/v1/healthcheck");
        register("Http Server", new HttpHealthCheck(host, configuration.tradeServerPort()));

        Disruptor<Message> disruptor = new Disruptor<>(FACTORY, 1024, INSTANCE, MULTI, new BlockingWaitStrategy());
        serviceConfiguration.discover(disruptor, "matchingengine");
        disruptor.start();

        final OrderServlet orderServlet = new OrderServlet(disruptor);
        context.addServlet(new ServletHolder(orderServlet), "/v1/order/new");

        final ExecutionServlet executionServlet = new ExecutionServlet(
                host,
                configuration.executionPort(),
                disruptor
        );
        final ServletHolder executionServletHolder = new ServletHolder(executionServlet);
        context.addServlet(executionServletHolder, "/v1/execution");

        final ServletHolder orderBookServletHolder = new ServletHolder(new OrderBookServlet());
        orderBookServletHolder.setAsyncSupported(true);
        orderBookServletHolder.setInitParameter("host", host);
        orderBookServletHolder.setInitParameter("port", Integer.toString(configuration.orderBookPort()));
        context.addServlet(orderBookServletHolder, "/v1/book/*");

        final ServletHolder blotterServletHolder = new ServletHolder(new BlotterServlet());
        blotterServletHolder.setAsyncSupported(true);
        blotterServletHolder.setInitParameter("host", host);
        blotterServletHolder.setInitParameter("port", Integer.toString(configuration.blotterPort()));
        context.addServlet(blotterServletHolder, "/v1/order/blotter");

        server.setHandler(context);

        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public interface ServerConfiguration {

        int tradeServerPort();

        int executionPort();

        int orderBookPort();

        int blotterPort();
    }
}
