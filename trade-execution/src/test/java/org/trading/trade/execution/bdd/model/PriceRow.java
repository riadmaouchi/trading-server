package org.trading.trade.execution.bdd.model;

public class PriceRow {
    public final String volume;
    public final String bid;
    public final String ask;

    public PriceRow(String volume, String bid, String ask) {
        this.volume = volume;
        this.bid = bid;
        this.ask = ask;
    }

}
