package org.trading.trade.execution;

public enum ServiceName {
    TRADESERVER("HTTP", 8080),
    EXECUTION("TCP", 8984),
    ORDERBOOK("TCP", 8982),
    BLOTTER("TCP", 8983);

    public final String protocol;
    public final int defaultPort;

    ServiceName(String protocol, int defaultPort) {
        this.protocol = protocol;
        this.defaultPort = defaultPort;
    }
}
