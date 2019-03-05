package org.trading.pricing.bdd.model;

public class Prices {
    public final String volume;
    public final String bid;
    public final String ask;

    public Prices(String volume, String bid, String ask) {
        this.volume = volume;
        this.bid = bid;
        this.ask = ask;
    }
}
