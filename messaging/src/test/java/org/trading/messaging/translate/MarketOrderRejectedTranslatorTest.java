package org.trading.messaging.translate;

import org.junit.jupiter.api.Test;
import org.trading.api.event.MarketOrderRejected;
import org.trading.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.MarketOrderRejectedBuilder.aMarketOrderRejected;
import static org.trading.messaging.Message.EventType.MARKET_ORDER_REJECTED;
import static org.trading.messaging.translate.MarketOrderRejectedTranslator.translateTo;

class MarketOrderRejectedTranslatorTest {

    @Test
    void translate_market_order_placed_event() {
        // Given
        MarketOrderRejected marketOrderRejected = aMarketOrderRejected().build();

        // When
        Message message = new Message();
        translateTo(message, 1L, marketOrderRejected);

        // Then
        assertThat(message.type).isEqualTo(MARKET_ORDER_REJECTED);
        assertThat(message.event).isEqualTo(marketOrderRejected);
    }

}