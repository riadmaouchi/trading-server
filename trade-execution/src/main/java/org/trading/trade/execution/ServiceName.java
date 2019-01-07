package org.trading.trade.execution;

public enum ServiceName {
    SERVER("tradeserver", "http"),
    EXECUTION("execution", "tcp"),
    ORDERBOOK("orderbook", "tcp"),
    BLOTTER("blotter", "tcp");

    public final String name;
    public final String protocol;

    ServiceName(String name, String protocol) {
        this.name = name;
        this.protocol = protocol;
    }
}
