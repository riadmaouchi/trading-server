package org.trading.discovery;

import org.trading.discovery.consul.ConsulProviderFactory;
import org.trading.health.HealthCheckServer;
import org.trading.serviceregistry.ServiceRegistry;

public interface RemoteProviderFactory {

    ServiceRegistry getServiceConfiguration();

    static RemoteProviderFactory getFactory(RemoteProvider remoteProvider, HealthCheckServer healthCheckServer,String consulUrl ) {
        return remoteProvider.accept(new RemoteProviderVisitor<>() {
            @Override
            public RemoteProviderFactory visitConsul() {
                return new ConsulProviderFactory(consulUrl, healthCheckServer);
            }

            @Override
            public RemoteProviderFactory visitDefault() {
                return new DefaultProviderFactory(healthCheckServer);
            }
        });
    }

    enum RemoteProvider {
        CONSUL {
            @Override
            public <R> R accept(RemoteProviderVisitor<R> visitor) {
                return visitor.visitConsul();
            }
        },
        DEFAULT {
            @Override
            public <R> R accept(RemoteProviderVisitor<R> visitor) {
                return visitor.visitDefault();
            }
        };

        public abstract <R> R accept(RemoteProviderVisitor<R> visitor);

    }

    interface RemoteProviderVisitor<R> {
        R visitConsul();

        R visitDefault();
    }
}
