package org.trading.matching.engine.bdd.model;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public final class Book {
    public final String buyBroker;
    public final String buyQuantity;
    public final String buyPrice;
    public final String sellPrice;
    public final String sellQuantity;
    public final String sellBroker;


    public Book(String buyBroker,
                String buyQuantity,
                String buyPrice,
                String sellPrice,
                String sellQuantity,
                String sellBroker) {
        this.buyBroker = buyBroker;
        this.buyQuantity = buyQuantity;
        this.buyPrice = buyPrice;
        this.sellBroker = sellBroker;
        this.sellQuantity = sellQuantity;
        this.sellPrice = sellPrice;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Book)) {
            return false;
        }
        Book that = (Book) other;
        return Objects.equals(buyBroker, that.buyBroker) &&
                Objects.equals(buyQuantity, that.buyQuantity) &&
                Objects.equals(buyPrice, that.buyPrice) &&
                Objects.equals(sellBroker, that.sellBroker) &&
                Objects.equals(sellQuantity, that.sellQuantity) &&
                Objects.equals(sellPrice, that.sellPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buyBroker, buyQuantity, buyPrice, sellBroker, sellQuantity, sellPrice);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("buyBroker", buyBroker)
                .add("buyQuantity", buyQuantity)
                .add("buyPrice", buyPrice)
                .add("sellBroker", sellBroker)
                .add("sellQuantity", sellQuantity)
                .add("sellPrice", sellPrice)
                .toString();
    }
}
