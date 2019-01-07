package org.trading.messaging.translate;

import org.junit.jupiter.api.Test;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.LimitOrderPlacedBuilder.aLimitOrderPlaced;
import static org.trading.messaging.Message.EventType.LIMIT_ORDER_PLACED;
import static org.trading.messaging.translate.LimitOrderPlacedTranslator.translateTo;

class LimitOrderPlacedTranslatorTest {

    @Test
    void translate_limit_order_placed_event() {
        // Given
        LimitOrderPlaced limitOrderPlaced = aLimitOrderPlaced().build();

        // When
        Message message = new Message();
        translateTo(message, 1L, limitOrderPlaced);

        // Then
        assertThat(message.type).isEqualTo(LIMIT_ORDER_PLACED);
        assertThat(message.event).isEqualTo(limitOrderPlaced);
    }

}