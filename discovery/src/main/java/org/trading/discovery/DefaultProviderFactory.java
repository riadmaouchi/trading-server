package org.trading.discovery;

import org.trading.serviceregistry.ServiceRegistry;
import org.trading.health.HealthCheckServer;

public class DefaultProviderFactory implements RemoteProviderFactory {
    private final HealthCheckServer healthCheckServer;

    public DefaultProviderFactory(HealthCheckServer healthCheckServer) {
        this.healthCheckServer = healthCheckServer;
    }

    @Override
    public ServiceRegistry getServiceConfiguration() {
        return new DefaultServiceRegistry(healthCheckServer);
    }
}
