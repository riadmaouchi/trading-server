package org.trading.api;

import org.trading.api.event.MarketOrderRejected;
import org.trading.api.message.FillStatus;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static org.trading.api.message.FillStatus.FULLY_FILLED;

public final class MarketOrderRejectedBuilder {
    private LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);
    private UUID id = new UUID(0, 1);
    private FillStatus fillStatus = FULLY_FILLED;

    private MarketOrderRejectedBuilder() {
    }

    public static MarketOrderRejectedBuilder aMarketOrderRejected() {
        return new MarketOrderRejectedBuilder();
    }

    public MarketOrderRejected build() {
        return new MarketOrderRejected(
                id,
                fillStatus,
                time
        );
    }

    public MarketOrderRejectedBuilder withTime(final LocalDateTime time) {
        this.time = time;
        return this;
    }

    public MarketOrderRejectedBuilder withId(final UUID id) {
        this.id = id;
        return this;
    }

    public MarketOrderRejectedBuilder withFillStatus(final FillStatus fillStatus) {
        this.fillStatus = fillStatus;
        return this;
    }

}
