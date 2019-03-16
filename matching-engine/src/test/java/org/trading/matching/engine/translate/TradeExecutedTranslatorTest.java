package org.trading.matching.engine.translate;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.trading.MessageProvider;
import org.trading.api.message.OrderType;
import org.trading.matching.engine.domain.Trade;
import org.trading.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.Month.JULY;
import static java.time.ZoneOffset.UTC;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.trading.matching.engine.TradeBuilder.aTrade;
import static org.trading.messaging.Message.EventType.TRADE_EXECUTED;

@ExtendWith(DataProviderExtension.class)
class TradeExecutedTranslatorTest {

    @Test
    void should_translate_trade_executed() {

        // Given
        LocalDateTime time = LocalDateTime.of(2018, JULY, 1, 17, 5, 38);

        Trade trade = aTrade()
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
        TradeExecutedTranslator.translateTo(message, 1L, trade);

        // Then
        assertThat(message.type).isEqualTo(TRADE_EXECUTED);
        MessageProvider.TradeExecuted expectedTradeExecuted = (MessageProvider.TradeExecuted) message.event;
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

    @DataProvider({
            "MARKET, MARKET",
            "LIMIT, LIMIT",
    })
    @TestTemplate
    void should_translate_trade_executed_order_types(OrderType inputOrderType, MessageProvider.OrderType outputOrderSide) {

        // Given
        Trade trade = aTrade()
                .withBuyingOrderType(inputOrderType)
                .withSellingOrderType(inputOrderType)
                .build();

        // When
        Message message = new Message();
        TradeExecutedTranslator.translateTo(message, 1L, trade);

        // Then
        MessageProvider.TradeExecuted expectedTradeExecuted = (MessageProvider.TradeExecuted) message.event;
        assertThat(expectedTradeExecuted.getBuyingOrderType()).isEqualTo(outputOrderSide);
        assertThat(expectedTradeExecuted.getSellingOrderType()).isEqualTo(outputOrderSide);

    }

}