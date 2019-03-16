package org.trading.messaging.translate;

import org.junit.jupiter.api.Test;
import org.trading.api.event.LimitOrderAccepted;
import org.trading.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.LimitOrderAcceptedBuilder.aLimitOrderAccepted;
import static org.trading.messaging.Message.EventType.LIMIT_ORDER_ACCEPTED;
import static org.trading.messaging.translate.LimitOrderPlacedTranslator.translateTo;

class LimitOrderAcceptedTranslatorTest {

    @Test
    void translate_limit_order_placed_event() {
        // Given
        LimitOrderAccepted limitOrderAccepted = aLimitOrderAccepted().build();

        // When
        Message message = new Message();
        translateTo(message, 1L, limitOrderAccepted);

        // Then
        assertThat(message.type).isEqualTo(LIMIT_ORDER_ACCEPTED);
        assertThat(message.event).isEqualTo(limitOrderAccepted);
    }

}