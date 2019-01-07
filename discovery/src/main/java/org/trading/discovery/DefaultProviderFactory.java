package org.trading.discovery;

public class DefaultProviderFactory implements RemoteProviderFactory {

    @Override
    public ServiceConfiguration getServiceConfiguration() {
        return new DefaultServiceConfiguration();
    }
}
