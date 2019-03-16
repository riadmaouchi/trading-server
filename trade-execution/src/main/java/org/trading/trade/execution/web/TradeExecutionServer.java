package org.trading.trade.execution.web;

import com.codahale.metrics.servlets.PingServlet;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.trading.serviceregistry.ServiceRegistry;
import org.trading.health.HealthCheckClient;
import org.trading.health.HealthCheckServer;
import org.trading.messaging.Message;
import org.trading.trade.execution.esp.web.ExecutionServlet;
import org.trading.trade.execution.order.web.BlotterServlet;
import org.trading.trade.execution.order.web.OrderBookServlet;
import org.trading.trade.execution.order.web.OrderServlet;

import java.util.List;

import static com.lmax.disruptor.dsl.ProducerType.MULTI;
import static com.lmax.disruptor.util.DaemonThreadFactory.INSTANCE;
import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;
import static org.eclipse.jetty.servlets.CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER;
import static org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_HEADERS_PARAM;
import static org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_METHODS_PARAM;
import static org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_ORIGINS_PARAM;
import static org.eclipse.jetty.servlets.CrossOriginFilter.CHAIN_PREFLIGHT_PARAM;
import static org.trading.messaging.Message.FACTORY;

public class TradeExecutionServer {

    private final ServerConfiguration configuration;
    private final ServiceRegistry serviceRegistry;
    private final String host;
    private final HealthCheckServer healthCheckServer;
    private Server server;

    public TradeExecutionServer(ServerConfiguration configuration,
                                ServiceRegistry serviceRegistry,
                                String host,
                                HealthCheckServer healthCheckServer) {
        this.configuration = configuration;
        this.serviceRegistry = serviceRegistry;
        this.host = host;
        this.healthCheckServer = healthCheckServer;
    }

    public void start() throws Exception {
        server = new Server(configuration.tradeServerPort());
        ServletContextHandler context = new ServletContextHandler(NO_SESSIONS);
        context.setContextPath("/");
        CrossOriginFilter filter = new CrossOriginFilter();
        FilterHolder filterHolder = new FilterHolder(filter);
        filterHolder.setInitParameter(ALLOWED_ORIGINS_PARAM, "*");
        filterHolder.setInitParameter(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        filterHolder.setInitParameter(ALLOWED_METHODS_PARAM, "OPTIONS,GET,POST,HEAD");
        filterHolder.setInitParameter(ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Cache-Control");
        filterHolder.setInitParameter(CHAIN_PREFLIGHT_PARAM, "false");
        context.addFilter(filterHolder, "/*", null);

        final ServletHolder pingServlet = new ServletHolder(new PingServlet());
        context.addServlet(pingServlet, "/v1/healthcheck");
        healthCheckServer.register("Http Server", new HealthCheckClient(host, configuration.tradeServerPort()));

        Disruptor<Message> disruptor = new Disruptor<>(FACTORY, 1024, INSTANCE, MULTI, new BlockingWaitStrategy());
        serviceRegistry.discover(disruptor, List::of,"matchingengine");
        disruptor.start();

        final OrderServlet orderServlet = new OrderServlet(disruptor);
        context.addServlet(new ServletHolder(orderServlet), "/v1/order/new");

        final ExecutionServlet executionServlet = new ExecutionServlet(
                host,
                configuration.executionPort(),
                disruptor,
                healthCheckServer);
        final ServletHolder executionServletHolder = new ServletHolder(executionServlet);
        executionServletHolder.setAsyncSupported(true);
        context.addServlet(executionServletHolder, "/v1/execution");


        final ServletHolder orderBookServletHolder = new ServletHolder(new OrderBookServlet(healthCheckServer));
        orderBookServletHolder.setAsyncSupported(true);
        orderBookServletHolder.setInitParameter("host", host);
        orderBookServletHolder.setInitParameter("port", Integer.toString(configuration.orderBookPort()));
        context.addServlet(orderBookServletHolder, "/v1/book/*");

        final ServletHolder blotterServletHolder = new ServletHolder(new BlotterServlet(healthCheckServer));
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
