package org.trading.matching.engine.translate;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.trading.api.command.Side;
import org.trading.api.event.MarketOrderPlaced;
import org.trading.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static java.time.ZoneOffset.UTC;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.trading.api.MarketOrderPlacedBuilder.aMarketOrderPlaced;

@ExtendWith(DataProviderExtension.class)
class MarketOrderPlacedTranslatorTest {

    @Test
    void should_translate_market_order_placed() {

        // Given
        LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);

        MarketOrderPlaced marketOrderPlaced = aMarketOrderPlaced()
                .withId(new UUID(0, 1))
                .withBroker("Broker")
                .withQuantity(1_000_000)
                .withSymbol("EURUSD")
                .withTime(time)
                .build();

        // When
        Message message = new Message();
        MarketOrderPlacedTranslator.translateTo(message, 1L, marketOrderPlaced);

        // Then
        assertThat(message.type).isEqualTo(Message.EventType.MARKET_ORDER_PLACED);
        org.trading.MarketOrderPlaced expectedMarketOrderPlaced = (org.trading.MarketOrderPlaced) message.event;
        assertThat(expectedMarketOrderPlaced.getId()).isEqualTo("00000000-0000-0000-0000-000000000001");
        assertThat(expectedMarketOrderPlaced.getBroker()).isEqualTo("Broker");
        assertThat(expectedMarketOrderPlaced.getQuantity()).isEqualTo(1_000_000);
        assertThat(expectedMarketOrderPlaced.getSymbol()).isEqualTo("EURUSD");
        assertThat(expectedMarketOrderPlaced.getTime().getSeconds()).isEqualTo(MILLISECONDS.toSeconds(time.toInstant(UTC).toEpochMilli()));
    }

    @DataProvider({
            "BUY, BUY",
            "SELL, SELL"
    })
    @TestTemplate
    void should_translate_market_order_placed_side(Side inputSide, org.trading.Side outputSide) {

        // Given
        MarketOrderPlaced marketOrderPlaced = aMarketOrderPlaced()
                .withSide(inputSide)
                .build();

        // When
        Message message = new Message();
        MarketOrderPlacedTranslator.translateTo(message, 1L, marketOrderPlaced);

        // Then
        org.trading.MarketOrderPlaced expectedMarketOrderPlaced = (org.trading.MarketOrderPlaced) message.event;
        AssertionsForClassTypes.assertThat(expectedMarketOrderPlaced.getSide()).isEqualTo(outputSide);
    }

}