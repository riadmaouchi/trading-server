package org.trading.matching.engine.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.MessageProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.trading.MessageProvider.EventType.LIMIT_ORDER_ACCEPTED;
import static org.trading.MessageProvider.EventType.MARKET_ORDER_ACCEPTED;
import static org.trading.MessageProvider.EventType.MARKET_ORDER_REJECTED;
import static org.trading.MessageProvider.EventType.TRADE_EXECUTED;
import static org.trading.messaging.LimitOrderAcceptedBuilder.aLimitOrderAccepted;
import static org.trading.messaging.MarketOrderAcceptedBuilder.aMarketOrderAccepted;
import static org.trading.messaging.MarketOrderRejectedBuilder.aMarketOrderRejected;
import static org.trading.messaging.TradeExecutedBuilder.aTradeExecuted;

class MessageSerializerTest {

    private MessageSerializer serializer;

    @BeforeEach
    void before() {
        serializer = new MessageSerializer();
    }

    @Test
    void serialize_and_deserialize_market_order_rejected_message() {

        // Given
        MessageProvider.Message message = MessageProvider.Message.newBuilder()
                .setEvenType(MARKET_ORDER_REJECTED)
                .setMarketOrderRejected(aMarketOrderRejected().build())
                .build();

        // When
        byte[] bytes = serializer.serialize(message);

        // Then
        MessageProvider.Message deserialize = serializer.deserialize(bytes);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(message);
    }

    @Test
    void serialize_and_deserialize_trade_executed_message() {

        // Given
        MessageProvider.Message message = MessageProvider.Message.newBuilder()
                .setEvenType(TRADE_EXECUTED)
                .setTradeExecuted(aTradeExecuted().build())
                .build();

        // When
        byte[] bytes = serializer.serialize(message);

        // Then
        MessageProvider.Message deserialize = serializer.deserialize(bytes);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(message);
    }

    @Test
    void serialize_and_deserialize_market_order_accepted_message() {

        // Given
        MessageProvider.Message message = MessageProvider.Message.newBuilder()
                .setEvenType(MARKET_ORDER_ACCEPTED)
                .setMarketOrderAccepted(aMarketOrderAccepted().build())
                .build();

        // When
        byte[] bytes = serializer.serialize(message);

        // Then
        MessageProvider.Message deserialize = serializer.deserialize(bytes);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(message);
    }

    @Test
    void serialize_and_deserialize_limit_order_accepted_message() {

        // Given
        MessageProvider.Message message = MessageProvider.Message.newBuilder()
                .setEvenType(LIMIT_ORDER_ACCEPTED)
                .setLimitOrderAccepted(aLimitOrderAccepted().build())
                .build();

        // When
        byte[] bytes = serializer.serialize(message);

        // Then
        MessageProvider.Message deserialize = serializer.deserialize(bytes);
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(message);
    }

}