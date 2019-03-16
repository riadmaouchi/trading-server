package org.trading.matching.engine.eventstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.matching.engine.domain.OrderBook.LimitOrderAccepted;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.matching.engine.LimitOrderBuilder.aLimitOrder;

class LimitOrderAcceptedSerializerTest {

    private LimitOrderAcceptedSerializer serializer;

    @BeforeEach
    void before() {
        serializer = new LimitOrderAcceptedSerializer();
    }

    @Test
    void serialize_and_deserialize() {

        // Given
        LimitOrderAccepted limitOrderAccepted = new LimitOrderAccepted(
                new UUID(0L, 1L),
                1L,
                aLimitOrder().build()
        );


        // When
        byte[] bytes = serializer.serialize(limitOrderAccepted);

        // Then
        LimitOrderAccepted deserialize = serializer.deserialize(bytes, null);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(limitOrderAccepted);
    }

}