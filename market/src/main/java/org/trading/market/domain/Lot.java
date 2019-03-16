package org.trading.market.domain;

public enum Lot {
    STANDARD(100_000L),
    MINI(10_000L),
    MICRO(1_000L),
    NANO(100L);

    public final long numberOfUnits;

    Lot(long numberOfUnits) {
        this.numberOfUnits = numberOfUnits;
    }
}
