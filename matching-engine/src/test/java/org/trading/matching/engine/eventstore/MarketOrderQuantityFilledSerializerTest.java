package org.trading.matching.engine.eventstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.matching.engine.domain.OrderBook;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.matching.engine.MarketOrderBuilder.aMarketOrder;

class MarketOrderQuantityFilledSerializerTest {
    private MarketOrderQuantityFilledSerializer serializer;

    @BeforeEach
    void before() {
        serializer = new MarketOrderQuantityFilledSerializer();
    }

    @Test
    void serialize_and_deserialize() {

        // Given
        OrderBook.MarketOrderQuantityFilled marketOrderQuantityFilled = new OrderBook.MarketOrderQuantityFilled(
                new UUID(0L, 1L),
                1L,
                aMarketOrder().build(),
                10
        );


        // When
        byte[] bytes = serializer.serialize(marketOrderQuantityFilled);

        // Then
        OrderBook.MarketOrderQuantityFilled deserialize = serializer.deserialize(bytes, null);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(marketOrderQuantityFilled);
    }
}