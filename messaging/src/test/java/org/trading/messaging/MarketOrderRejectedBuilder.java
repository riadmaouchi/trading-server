package org.trading.messaging;

import com.google.protobuf.Timestamp;
import org.trading.MessageProvider.FillStatus;
import org.trading.MessageProvider.MarketOrderRejected;

import static org.trading.MessageProvider.FillStatus.FULLY_FILLED;

public final class MarketOrderRejectedBuilder {
    private Timestamp time = Timestamp.newBuilder().setSeconds(10000000L).build();
    private String id = "00000000-0000-0000-0000-000000000001";
    private FillStatus fillStatus = FULLY_FILLED;

    private MarketOrderRejectedBuilder() {
    }

    public static MarketOrderRejectedBuilder aMarketOrderRejected() {
        return new MarketOrderRejectedBuilder();
    }

    public MarketOrderRejected build() {
        return MarketOrderRejected.newBuilder()
                .setId(id)
                .setTime(time)
                .setFillStatus(fillStatus)
                .build();
    }

    public MarketOrderRejectedBuilder withTime(final Timestamp time) {
        this.time = time;
        return this;
    }

    public MarketOrderRejectedBuilder withId(final String id) {
        this.id = id;
        return this;
    }

    public MarketOrderRejectedBuilder withFillStatus(final FillStatus fillStatus) {
        this.fillStatus = fillStatus;
        return this;
    }

}
