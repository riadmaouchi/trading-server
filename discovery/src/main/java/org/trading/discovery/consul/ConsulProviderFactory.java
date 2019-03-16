package org.trading.discovery.consul;

import org.trading.discovery.RemoteProviderFactory;
import org.trading.serviceregistry.ServiceRegistry;
import org.trading.health.HealthCheckServer;

public class ConsulProviderFactory implements RemoteProviderFactory {
    private final ConsulService consulService;
    private final HealthCheckServer healthCheckServer;

    public ConsulProviderFactory(String host, HealthCheckServer healthCheckServer) {
        this.consulService = new ConsulService(host);
        this.healthCheckServer = healthCheckServer;
    }

    @Override
    public ServiceRegistry getServiceConfiguration() {
        return new ConsulServiceRegistry(consulService, healthCheckServer);
    }
}
