package org.trading.pricing.domain;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class Prices {
    public final String symbol;
    public final LocalDateTime time;
    public final double midMarketPrice;
    public final List<Price> bids = new ArrayList<>();
    public final List<Price> asks = new ArrayList<>();

    public Prices(String symbol,
                  LocalDateTime time,
                  List<Price> bids,
                  List<Price> asks, double midMarketPrice) {
        this.symbol = symbol;
        this.time = time;
        this.midMarketPrice = midMarketPrice;
        this.bids.addAll(bids);
        this.asks.addAll(asks);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("symbol", symbol)
                .add("time", time)
                .add("mid", midMarketPrice)
                .add("bids", bids)
                .add("asks", asks)
                .toString();
    }
}
