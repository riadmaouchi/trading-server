package org.trading.messaging.serializer;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.MessageProvider;
import org.trading.MessageProvider.Side;
import org.trading.api.event.LimitOrderAccepted;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.message.Side.BUY;
import static org.trading.messaging.LimitOrderAcceptedBuilder.aLimitOrderAccepted;

class LimitOrderAcceptedFromProtobufTest {

    private LimitOrderAcceptedFromProtobuf limitOrderAcceptedFromProtobuf;

    @BeforeEach
    void before() {
        limitOrderAcceptedFromProtobuf = new LimitOrderAcceptedFromProtobuf(new SideFromProtobuf());
    }

    @Test
    void should_convert_limit_order_accepted() {

        // Given
        MessageProvider.LimitOrderAccepted limitOrderAccepted = aLimitOrderAccepted()
                .withPrice(1.372)
                .withBroker("broker")
                .withSymbol("EURUSD")
                .withSide(Side.BUY)
                .withId("00000000-0000-0001-0000-000000000002")
                .withQuantity(5_000)
                .withTime(Timestamp.newBuilder().setSeconds(1530464460L).build())
                .build();

        // When
        Either<String, LimitOrderAccepted> limitOrderAcceptedEither = limitOrderAcceptedFromProtobuf.fromProtobuf(limitOrderAccepted);

        // then
        LimitOrderAccepted orderAccepted = limitOrderAcceptedEither.right();
        assertThat(orderAccepted.symbol).isEqualTo("EURUSD");
        assertThat(orderAccepted.quantity).isEqualTo(5_000);
        assertThat(orderAccepted.price).isEqualTo(1.372);
        assertThat(orderAccepted.broker).isEqualTo("broker");
        assertThat(orderAccepted.id).isEqualTo(new UUID(1, 2));
        assertThat(orderAccepted.side).isEqualTo(BUY);
        assertThat(orderAccepted.time).isEqualTo(LocalDateTime.of(2018, JULY, 1, 17, 1));
    }

}