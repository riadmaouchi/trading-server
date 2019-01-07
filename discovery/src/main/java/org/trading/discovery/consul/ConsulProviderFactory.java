package org.trading.discovery.consul;

import org.trading.discovery.RemoteProviderFactory;
import org.trading.discovery.ServiceConfiguration;

public class ConsulProviderFactory implements RemoteProviderFactory {

    private final ConsulService consulService;

    public ConsulProviderFactory() {
        this.consulService = new ConsulService();
    }

    @Override
    public ServiceConfiguration getServiceConfiguration() {
        return new ConsulServiceConfiguration(consulService);
    }
}
