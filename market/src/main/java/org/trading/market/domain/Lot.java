package org.trading.market.domain;

public enum Lot {
    STANDARD(100_000),
    MINI(10_000),
    MICRO(1_000),
    NANO(100);

    public final int numberOfUnits;

    Lot(int numberOfUnits) {
        this.numberOfUnits = numberOfUnits;
    }
}
