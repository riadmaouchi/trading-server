package org.trading.messaging.serializer;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.Side;
import org.trading.api.event.LimitOrderPlaced;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.command.Side.BUY;
import static org.trading.messaging.LimitOrderPlacedBuilder.aLimitOrderPlaced;

class LimitOrderPlacedFromProtobufTest {

    private LimitOrderPlacedFromProtobuf limitOrderPlacedFromProtobuf;

    @BeforeEach
    void before() {
        limitOrderPlacedFromProtobuf = new LimitOrderPlacedFromProtobuf(new SideFromProtobuf());
    }

    @Test
    void should_convert_limit_order_placed() {

        // Given
        org.trading.LimitOrderPlaced limitOrderPlaced = aLimitOrderPlaced()
                .withPrice(1.372)
                .withBroker("broker")
                .withSymbol("EURUSD")
                .withSide(Side.BUY)
                .withId("00000000-0000-0001-0000-000000000002")
                .withQuantity(5_000)
                .withTime(Timestamp.newBuilder().setSeconds(1530464460L).build())
                .build();

        // When
        Either<String, LimitOrderPlaced> limitOrderPlacedEither = limitOrderPlacedFromProtobuf.fromProtobuf(limitOrderPlaced);

        // then
        LimitOrderPlaced orderPlaced = limitOrderPlacedEither.right();
        assertThat(orderPlaced.symbol).isEqualTo("EURUSD");
        assertThat(orderPlaced.quantity).isEqualTo(5_000);
        assertThat(orderPlaced.price).isEqualTo(1.372);
        assertThat(orderPlaced.broker).isEqualTo("broker");
        assertThat(orderPlaced.id).isEqualTo(new UUID(1, 2));
        assertThat(orderPlaced.side).isEqualTo(BUY);
        assertThat(orderPlaced.time).isEqualTo(LocalDateTime.of(2018, JULY, 1, 17, 1));
    }

}