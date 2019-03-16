package org.trading.discovery;

import org.trading.discovery.consul.ConsulProviderFactory;
import org.trading.serviceregistry.ServiceRegistry;
import org.trading.health.HealthCheckServer;

import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;

public interface RemoteProviderFactory {

    ServiceRegistry getServiceConfiguration();

    static RemoteProviderFactory getFactory(RemoteProvider remoteProvider, HealthCheckServer healthCheckServer) {
        return remoteProvider.accept(new RemoteProviderVisitor<>() {
            @Override
            public RemoteProviderFactory visitConsul() {
                return new ConsulProviderFactory(ofNullable(getenv("CONSUL.URL")).orElse("localhost"), healthCheckServer);
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
