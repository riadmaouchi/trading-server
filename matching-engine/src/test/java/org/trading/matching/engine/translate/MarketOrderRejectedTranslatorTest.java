package org.trading.matching.engine.translate;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.trading.MessageProvider;
import org.trading.api.message.FillStatus;
import org.trading.matching.engine.domain.MarketOrder;
import org.trading.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static java.time.ZoneOffset.UTC;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.trading.matching.engine.MarketOrderBuilder.aMarketOrder;
import static org.trading.messaging.Message.EventType.MARKET_ORDER_REJECTED;

@ExtendWith(DataProviderExtension.class)
class MarketOrderRejectedTranslatorTest {

    @Test
    void should_translate_market_order_rejected() {

        // Given
        LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);

        MarketOrder marketOrder = aMarketOrder()
                .withId(new UUID(0, 2))
                .withTime(time)
                .build();


        // When
        Message message = new Message();
        MarketOrderRejectedTranslator.translateTo(message, 1L, marketOrder, FillStatus.FULLY_FILLED);

        // Then
        assertThat(message.type).isEqualTo(MARKET_ORDER_REJECTED);
        MessageProvider.MarketOrderRejected marketOrderRejected = (MessageProvider.MarketOrderRejected) message.event;
        assertThat(marketOrderRejected.getId()).isEqualTo("00000000-0000-0000-0000-000000000002");
        assertThat(marketOrderRejected.getTime().getSeconds()).isEqualTo(MILLISECONDS.toSeconds(time.toInstant(UTC).toEpochMilli()));
    }

    @DataProvider({
            "FULLY_FILLED, FULLY_FILLED",
            "PARTIALLY_FILLED, PARTIALLY_FILLED"
    })
    @TestTemplate
    void should_translate_market_order_rejected_status(FillStatus inputFillStatus, MessageProvider.FillStatus outputFillStatus) {

        // When
        Message message = new Message();
        MarketOrderRejectedTranslator.translateTo(message, 1L, aMarketOrder().build(), inputFillStatus);

        // Then
        MessageProvider.MarketOrderRejected marketOrderRejected = (MessageProvider.MarketOrderRejected) message.event;
        assertThat(marketOrderRejected.getFillStatus()).isEqualTo(outputFillStatus);
    }

}