package org.trading.pricing.domain;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public final class Price {
    public final int liquidity;
    public final double price;

    public Price(int liquidity, double price) {
        this.liquidity = liquidity;
        this.price = price;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Price)) {
            return false;
        }
        Price that = (Price) other;
        return liquidity == that.liquidity &&
                Double.compare(that.price, price) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(liquidity, price);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("liquidity", liquidity)
                .add("price", price)
                .toString();
    }
}
