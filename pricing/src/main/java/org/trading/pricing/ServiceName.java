package org.trading.pricing;

public enum ServiceName {
    SERVER("HTTP", 8085),
    PRICING("TCP", 8981);

    public final String protocol;
    public final int defaultPort;

    ServiceName(String protocol, int defaultPort) {
        this.protocol = protocol;
        this.defaultPort = defaultPort;
    }
}
