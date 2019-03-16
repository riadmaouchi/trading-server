package org.trading.matching.engine.eventstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.matching.engine.domain.OrderBook;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.matching.engine.LimitOrderBuilder.aLimitOrder;

class LimitOrderQuantityFilledSerializerTest {
    private LimitOrderQuantityFilledSerializer serializer;

    @BeforeEach
    void before() {
        serializer = new LimitOrderQuantityFilledSerializer();
    }

    @Test
    void serialize_and_deserialize() {

        // Given
        OrderBook.LimitOrderQuantityFilled limitOrderQuantityFilled = new OrderBook.LimitOrderQuantityFilled(
                new UUID(0L, 1L),
                1L,
                aLimitOrder().build(),
                10
        );


        // When
        byte[] bytes = serializer.serialize(limitOrderQuantityFilled);

        // Then
        OrderBook.LimitOrderQuantityFilled deserialize = serializer.deserialize(bytes, null);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(limitOrderQuantityFilled);
    }
}