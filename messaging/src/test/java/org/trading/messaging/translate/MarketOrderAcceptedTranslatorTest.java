package org.trading.messaging.translate;

import org.junit.jupiter.api.Test;
import org.trading.api.event.MarketOrderAccepted;
import org.trading.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.api.MarketOrderAcceptedBuilder.aMarketOrderAccepted;
import static org.trading.messaging.Message.EventType.MARKET_ORDER_ACCEPTED;
import static org.trading.messaging.translate.MarketOrderPlacedTranslator.translateTo;

class MarketOrderAcceptedTranslatorTest {

    @Test
    void translate_market_order_placed_event() {
        // Given
        MarketOrderAccepted marketOrderPlaced = aMarketOrderAccepted().build();

        // When
        Message message = new Message();
        translateTo(message, 1L, marketOrderPlaced);

        // Then
        assertThat(message.type).isEqualTo(MARKET_ORDER_ACCEPTED);
        assertThat(message.event).isEqualTo(marketOrderPlaced);
    }

}