package org.trading.pricing.web;

import com.codahale.metrics.servlets.PingServlet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.trading.discovery.ServiceConfiguration;
import org.trading.health.HttpHealthCheck;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;
import static org.eclipse.jetty.servlets.CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER;
import static org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_HEADERS_PARAM;
import static org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_METHODS_PARAM;
import static org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_ORIGINS_PARAM;
import static org.eclipse.jetty.servlets.CrossOriginFilter.CHAIN_PREFLIGHT_PARAM;
import static org.trading.health.HealthCheckServer.register;

public class PricingServer {
    private final ServiceConfiguration serviceConfiguration;
    private final String host;
    private final PricingServerConfiguration pricingServerConfiguration;
    private Server server;

    public PricingServer(ServiceConfiguration serviceConfiguration,
                         String host,
                         PricingServerConfiguration pricingServerConfiguration) {
        this.serviceConfiguration = serviceConfiguration;
        this.host = host;
        this.pricingServerConfiguration = pricingServerConfiguration;
        register("Http Server", new HttpHealthCheck(host, pricingServerConfiguration.getHttpPort()));
    }

    public void start() throws Exception {
        server = new Server(pricingServerConfiguration.getHttpPort());
        ServletContextHandler context = new ServletContextHandler(NO_SESSIONS);
        context.setContextPath("/");

        FilterHolder filterHolder = new FilterHolder(new CrossOriginFilter());
        filterHolder.setInitParameter(ALLOWED_ORIGINS_PARAM, "*");
        filterHolder.setInitParameter(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        filterHolder.setInitParameter(ALLOWED_METHODS_PARAM, "OPTIONS,GET,POST,HEAD");
        filterHolder.setInitParameter(ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Cache-Control");
        filterHolder.setInitParameter(CHAIN_PREFLIGHT_PARAM, "false");
        context.addFilter(filterHolder, "/*", null);

        server.setHandler(context);
        context.addServlet(new ServletHolder(new PingServlet()), "/v1/healthcheck");


        final PricingServlet pricingServlet = new PricingServlet(
                new IntArrayList(new int[]{1_000_000, 5_000_000, 10_000_000, 25_000_000, 50_000_000}),
                serviceConfiguration
        );

        final ServletHolder pricingServletHolder = new ServletHolder(pricingServlet);
        pricingServletHolder.setInitParameter("host", host);
        pricingServletHolder.setInitParameter("port", Integer.toString(pricingServerConfiguration.getPricingPort()));
        pricingServletHolder.setAsyncSupported(true);
        context.addServlet(pricingServletHolder, "/v1/pricing/*");
        server.setHandler(context);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public interface PricingServerConfiguration {
        int getHttpPort();

        int getPricingPort();
    }
}
