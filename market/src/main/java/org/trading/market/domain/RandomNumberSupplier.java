package org.trading.market.domain;

import java.util.Random;
import java.util.function.DoubleSupplier;

import static java.lang.System.nanoTime;

public final class RandomNumberSupplier implements DoubleSupplier {

    private final double lowerBound;
    private final double upperBound;
    private final Random random;

    public RandomNumberSupplier(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        random = new Random(nanoTime());
    }

    @Override
    public double getAsDouble() {
        return random.nextDouble() * (upperBound - lowerBound) + lowerBound;
    }
}
