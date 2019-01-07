package org.trading.messaging.translate;

import org.junit.jupiter.api.Test;
import org.trading.api.command.SubmitOrder;
import org.trading.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.SubmitOrderBuilder.aSubmitOrder;
import static org.trading.messaging.Message.EventType.SUBMIT_ORDER;
import static org.trading.messaging.translate.SubmitOrderTranslator.translateTo;

class SubmitOrderTranslatorTest {

    @Test
    void translate_submit_order_event() {
        // Given
        SubmitOrder submitOrder = aSubmitOrder();

        // When
        Message message = new Message();
        translateTo(message, 1L, submitOrder);

        // Then
        assertThat(message.type).isEqualTo(SUBMIT_ORDER);
        assertThat(message.event).isEqualTo(submitOrder);
    }

}