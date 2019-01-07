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
        context.addFilter(new FilterHolder(new CrossOriginFilter()), "/*", null);

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
