package org.trading.messaging.translate;

import org.junit.jupiter.api.Test;
import org.trading.api.event.TradeExecuted;
import org.trading.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.TradeExecutedBuilder.aTradeExecuted;
import static org.trading.messaging.Message.EventType.TRADE_EXECUTED;
import static org.trading.messaging.translate.TradeExecutedTranslator.translateTo;

class TradeExecutedTranslatorTest {

    @Test
    void translate_submit_order_event() {
        // Given
        TradeExecuted tradeExecuted = aTradeExecuted().build();

        // When
        Message message = new Message();
        translateTo(message, 1L, tradeExecuted);

        // Then
        assertThat(message.type).isEqualTo(TRADE_EXECUTED);
        assertThat(message.event).isEqualTo(tradeExecuted);

    }

}