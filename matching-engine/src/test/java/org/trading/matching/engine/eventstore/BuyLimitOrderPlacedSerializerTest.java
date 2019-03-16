package org.trading.matching.engine.eventstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.matching.engine.domain.OrderBook.BuyLimitOrderPlaced;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.matching.engine.LimitOrderBuilder.aLimitOrder;

class BuyLimitOrderPlacedSerializerTest {

    private BuyLimitOrderPlacedSerializer serializer;

    @BeforeEach
    void before() {
        serializer = new BuyLimitOrderPlacedSerializer();
    }

    @Test
    void serialize_and_deserialize() {

        // Given
        BuyLimitOrderPlaced buyLimitOrderPlaced = new BuyLimitOrderPlaced(
                new UUID(0L, 1L),
                1L,
                aLimitOrder().build()
        );


        // When
        byte[] bytes = serializer.serialize(buyLimitOrderPlaced);

        // Then
        BuyLimitOrderPlaced deserialize = serializer.deserialize(bytes, null);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(buyLimitOrderPlaced);
    }

}