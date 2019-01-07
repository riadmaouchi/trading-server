package org.trading.pricing;

public enum ServiceName {
    SERVER("server", "http"),
    PRICING("pricing", "tcp");

    public final String name;
    public final String protocol;

    ServiceName(String name, String protocol) {
        this.name = name;
        this.protocol = protocol;
    }
}
