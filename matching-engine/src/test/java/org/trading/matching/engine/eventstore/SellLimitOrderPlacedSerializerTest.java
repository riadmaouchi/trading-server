package org.trading.matching.engine.eventstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.matching.engine.domain.OrderBook.SellLimitOrderPlaced;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.matching.engine.LimitOrderBuilder.aLimitOrder;

class SellLimitOrderPlacedSerializerTest {

    private SellLimitOrderPlacedSerializer serializer;

    @BeforeEach
    void before() {
        serializer = new SellLimitOrderPlacedSerializer();
    }

    @Test
    void serialize_and_deserialize() {

        // Given
        SellLimitOrderPlaced buyLimitOrderPlaced = new SellLimitOrderPlaced(
                new UUID(0L, 1L),
                1L,
                aLimitOrder().build()
        );

        // When
        byte[] bytes = serializer.serialize(buyLimitOrderPlaced);

        // Then
        SellLimitOrderPlaced deserialize = serializer.deserialize(bytes, null);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(buyLimitOrderPlaced);
    }

}