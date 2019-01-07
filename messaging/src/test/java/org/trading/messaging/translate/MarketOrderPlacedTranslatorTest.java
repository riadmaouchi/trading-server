package org.trading.messaging.translate;

import org.junit.jupiter.api.Test;
import org.trading.api.event.MarketOrderPlaced;
import org.trading.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.MarketOrderPlacedBuilder.aMarketOrderPlaced;
import static org.trading.messaging.Message.EventType.MARKET_ORDER_PLACED;
import static org.trading.messaging.translate.MarketOrderPlacedTranslator.translateTo;

class MarketOrderPlacedTranslatorTest {

    @Test
    void translate_market_order_placed_event() {
        // Given
        MarketOrderPlaced marketOrderPlaced = aMarketOrderPlaced().build();

        // When
        Message message = new Message();
        translateTo(message, 1L, marketOrderPlaced);

        // Then
        assertThat(message.type).isEqualTo(MARKET_ORDER_PLACED);
        assertThat(message.event).isEqualTo(marketOrderPlaced);
    }

}