package org.trading.discovery;

import org.trading.discovery.consul.ConsulProviderFactory;

public interface RemoteProviderFactory {

    ServiceConfiguration getServiceConfiguration();

    static RemoteProviderFactory getFactory(RemoteProvider remoteProvider) {
        return remoteProvider.accept(new RemoteProviderVisitor<>() {
            @Override
            public RemoteProviderFactory visitConsul() {
                return new ConsulProviderFactory();
            }

            @Override
            public RemoteProviderFactory visitDefault() {
                return new DefaultProviderFactory();
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
