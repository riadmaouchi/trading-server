package org.trading.matching.engine.translate;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.trading.MessageProvider;
import org.trading.MessageProvider.LimitOrderAccepted;
import org.trading.api.message.Side;
import org.trading.matching.engine.domain.LimitOrder;
import org.trading.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static java.time.ZoneOffset.UTC;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.trading.matching.engine.LimitOrderBuilder.aLimitOrder;

@ExtendWith(DataProviderExtension.class)
class LimitOrderAcceptedTranslatorTest {

    @Test
    void should_translate_limit_order_accepted() {

        // Given
        LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);

        LimitOrder limitOrder = aLimitOrder()
                .withId(new UUID(0, 1))
                .withBroker("Broker")
                .withQuantity(1_000_000)
                .withLimit(1.23482)
                .withSymbol("EURUSD")
                .withTime(time)
                .build();

        // When
        Message message = new Message();
        LimitOrderAcceptedTranslator.translateTo(message, 1L, limitOrder);

        // Then
        assertThat(message.type).isEqualTo(Message.EventType.LIMIT_ORDER_ACCEPTED);
        MessageProvider.LimitOrderAccepted expectedLimitOrderPlaced = (MessageProvider.LimitOrderAccepted) message.event;
        assertThat(expectedLimitOrderPlaced.getId()).isEqualTo("00000000-0000-0000-0000-000000000001");
        assertThat(expectedLimitOrderPlaced.getBroker()).isEqualTo("Broker");
        assertThat(expectedLimitOrderPlaced.getQuantity()).isEqualTo(1_000_000);
        assertThat(expectedLimitOrderPlaced.getSymbol()).isEqualTo("EURUSD");
        assertThat(expectedLimitOrderPlaced.getLimit()).isEqualTo(1.23482);
        assertThat(expectedLimitOrderPlaced.getTime().getSeconds()).isEqualTo(MILLISECONDS.toSeconds(time.toInstant(UTC).toEpochMilli()));
    }

    @DataProvider({
            "BUY, BUY",
            "SELL, SELL"
    })
    @TestTemplate
    void should_translate_limit_order_accepted_side(Side inputSide, MessageProvider.Side outputSide) {

        // Given
        LimitOrder limitOrder = aLimitOrder()
                .withSide(inputSide)
                .build();

        // When
        Message message = new Message();
        LimitOrderAcceptedTranslator.translateTo(message, 1L, limitOrder);

        // Then
        LimitOrderAccepted expectedLimitOrderPlaced = (LimitOrderAccepted) message.event;
        assertThat(expectedLimitOrderPlaced.getSide()).isEqualTo(outputSide);
    }

}