package org.trading.matching.engine.translate;

import org.junit.jupiter.api.Test;
import org.trading.api.event.TradeExecuted;
import org.trading.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static java.time.ZoneOffset.UTC;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.trading.api.TradeExecutedBuilder.aTradeExecuted;

class TradeExecutedTranslatorTest {

    @Test
    void should_translate_trade_executed() {

        // Given
        LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);

        TradeExecuted tradeExecuted = aTradeExecuted()
                .withBuyingId(new UUID(0, 1))
                .withBuyingBroker("Buying Broker")
                .withBuyingLimit(1.23489)
                .withSellingId(new UUID(2, 3))
                .withSellingBroker("Selling Broker")
                .withSellingLimit(1.23480)
                .withPrice(1.23482)
                .withQuantity(1_000_000)
                .withTime(time)
                .withSymbol("EURUSD")
                .build();

        // When
        Message message = new Message();
        TradeExecutedTranslator.translateTo(message, 1L, tradeExecuted);

        // Then
        assertThat(message.type).isEqualTo(Message.EventType.TRADE_EXECUTED);
        org.trading.TradeExecuted expectedTradeExecuted = (org.trading.TradeExecuted) message.event;
        assertThat(expectedTradeExecuted.getBuyingId()).isEqualTo("00000000-0000-0000-0000-000000000001");
        assertThat(expectedTradeExecuted.getBuyingBroker()).isEqualTo("Buying Broker");
        assertThat(expectedTradeExecuted.getBuyingLimit()).isEqualTo(1.23489);

        assertThat(expectedTradeExecuted.getSellingId()).isEqualTo("00000000-0000-0002-0000-000000000003");
        assertThat(expectedTradeExecuted.getSellingBroker()).isEqualTo("Selling Broker");
        assertThat(expectedTradeExecuted.getSellingLimit()).isEqualTo(1.23480);

        assertThat(expectedTradeExecuted.getQuantity()).isEqualTo(1_000_000);
        assertThat(expectedTradeExecuted.getSymbol()).isEqualTo("EURUSD");
        assertThat(expectedTradeExecuted.getTime().getSeconds()).isEqualTo(MILLISECONDS.toSeconds(time.toInstant(UTC).toEpochMilli()));
    }

}