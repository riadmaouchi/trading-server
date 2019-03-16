package org.trading.matching.engine.translate;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.trading.MessageProvider;
import org.trading.api.message.Side;
import org.trading.matching.engine.domain.MarketOrder;
import org.trading.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static java.time.ZoneOffset.UTC;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.trading.matching.engine.MarketOrderBuilder.aMarketOrder;
import static org.trading.messaging.Message.EventType.MARKET_ORDER_ACCEPTED;

@ExtendWith(DataProviderExtension.class)
class MarketOrderAcceptedTranslatorTest {

    @Test
    void should_translate_market_order_accepted() {

        // Given
        LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);

        MarketOrder marketOrder = aMarketOrder()
                .withId(new UUID(0, 1))
                .withBroker("Broker")
                .withQuantity(1_000_000)
                .withSymbol("EURUSD")
                .withTime(time)
                .build();

        // When
        Message message = new Message();
        MarketOrderAcceptedTranslator.translateTo(message, 1L, marketOrder);

        // Then
        assertThat(message.type).isEqualTo(MARKET_ORDER_ACCEPTED);
        MessageProvider.MarketOrderAccepted expectedMarketOrderAccepted = (MessageProvider.MarketOrderAccepted) message.event;
        assertThat(expectedMarketOrderAccepted.getId()).isEqualTo("00000000-0000-0000-0000-000000000001");
        assertThat(expectedMarketOrderAccepted.getBroker()).isEqualTo("Broker");
        assertThat(expectedMarketOrderAccepted.getQuantity()).isEqualTo(1_000_000);
        assertThat(expectedMarketOrderAccepted.getSymbol()).isEqualTo("EURUSD");
        assertThat(expectedMarketOrderAccepted.getTime().getSeconds()).isEqualTo(MILLISECONDS.toSeconds(time.toInstant(UTC).toEpochMilli()));
    }

    @DataProvider({
            "BUY, BUY",
            "SELL, SELL"
    })
    @TestTemplate
    void should_translate_market_order_accepted_side(Side inputSide, MessageProvider.Side outputSide) {

        // Given
        MarketOrder marketOrder = aMarketOrder()
                .withSide(inputSide)
                .build();

        // When
        Message message = new Message();
        MarketOrderAcceptedTranslator.translateTo(message, 1L, marketOrder);

        // Then
        MessageProvider.MarketOrderAccepted expectedMarketOrderAccepted = (MessageProvider.MarketOrderAccepted) message.event;
        assertThat(expectedMarketOrderAccepted.getSide()).isEqualTo(outputSide);
    }

}