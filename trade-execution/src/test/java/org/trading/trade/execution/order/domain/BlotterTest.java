package org.trading.trade.execution.order.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.api.event.MarketOrderPlaced;
import org.trading.api.event.TradeExecuted;
import org.trading.trade.execution.order.event.OrderUpdated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.time.Month.JULY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.trading.api.LimitOrderPlacedBuilder.aLimitOrderPlaced;
import static org.trading.api.MarketOrderPlacedBuilder.aMarketOrderPlaced;
import static org.trading.api.command.Side.BUY;
import static org.trading.api.command.Side.SELL;
import static org.trading.trade.execution.TradeExecutedBuilder.aTradeExecuted;
import static org.trading.trade.execution.order.event.OrderUpdated.Status.*;
import static org.trading.trade.execution.order.event.OrderUpdated.Type.LIMIT;
import static org.trading.trade.execution.order.event.OrderUpdated.Type.MARKET;

class BlotterTest {

    private OrderListener orderListener;
    private Blotter blotter;
    private ArgumentCaptor<OrderUpdated> captor;

    @BeforeEach
    void before() {
        orderListener = mock(OrderListener.class);
        blotter = new Blotter(orderListener);
        captor = forClass(OrderUpdated.class);
    }

    @Test
    void should_update_order_on_limit_order_placed() {

        // Given
        LimitOrderPlaced limitOrderPlaced = aLimitOrderPlaced()
                .withId(new UUID(0, 1))
                .withTime(LocalDateTime.of(2018, JULY, 1, 17, 1))
                .withSymbol("EURUSD")
                .withSide(BUY)
                .withPrice(1.3235)
                .withBroker("BROKER")
                .withQuantity(1_000)
                .build();

        // When
        blotter.onLimitOrderPlaced(limitOrderPlaced);

        // Then
        verify(orderListener).onOrderUpdated(captor.capture());
        OrderUpdated orderUpdated = captor.getValue();
        assertThat(orderUpdated.id).isEqualTo(new UUID(0, 1));
        assertThat(orderUpdated.time).isEqualTo(LocalDateTime.of(2018, JULY, 1, 17, 1));
        assertThat(orderUpdated.symbol).isEqualTo("EURUSD");
        assertThat(orderUpdated.direction).isEqualTo(BUY);
        assertThat(orderUpdated.requestedAmount).isEqualTo(1_000);
        assertThat(orderUpdated.leftAmount).isEqualTo(1_000);
        assertThat(orderUpdated.amount).isEqualTo(0);
        assertThat(orderUpdated.limit).isEqualTo(1.3235);
        assertThat(orderUpdated.price).isEqualTo(0);
        assertThat(orderUpdated.status).isEqualTo(SUBMITTING);
        assertThat(orderUpdated.type).isEqualTo(LIMIT);
        assertThat(orderUpdated.broker).isEqualTo("BROKER");
    }

    @Test
    void should_update_orders_on_execution() {

        // Given
        MarketOrderPlaced marketOrderPlaced = aMarketOrderPlaced()
                .withId(new UUID(0, 1))
                .withTime(LocalDateTime.of(2018, JULY, 1, 17, 1))
                .withSymbol("EURUSD")
                .withSide(BUY)
                .withBroker("BUYER")
                .withQuantity(1_000)
                .build();

        LimitOrderPlaced limitOrderPlaced = aLimitOrderPlaced()
                .withId(new UUID(0, 2))
                .withTime(LocalDateTime.of(2018, JULY, 1, 17, 2))
                .withSymbol("EURUSD")
                .withSide(SELL)
                .withPrice(1.3235)
                .withBroker("SELLER")
                .withQuantity(2_000)
                .build();
        blotter.onMarketOrderPlaced(marketOrderPlaced);
        blotter.onLimitOrderPlaced(limitOrderPlaced);
        reset(orderListener);

        TradeExecuted tradeExecuted = aTradeExecuted()
                .withBuyingId(new UUID(0, 1))
                .withSellingId(new UUID(0, 2))
                .withSymbol("EURUSD")
                .withTime(LocalDateTime.of(2018, JULY, 1, 17, 3))
                .withBuyingBroker("BUYER")
                .withSellingBroker("SELLER")
                .withSellingLimit(1.3235)
                .withQuantity(1_000)
                .withPrice(1.3235)
                .build();

        // When
        blotter.onTradeExecuted(tradeExecuted);

        // Then
        verify(orderListener, times(2)).onOrderUpdated(captor.capture());
        List<OrderUpdated> orders = captor.getAllValues();

        assertThat(orders).extracting(order -> order.id)
                .containsExactly(new UUID(0, 1), new UUID(0, 2));
        assertThat(orders).extracting(order -> order.time)
                .containsExactly(LocalDateTime.of(2018, JULY, 1, 17, 1), LocalDateTime.of(2018, JULY, 1, 17, 2));
        assertThat(orders).extracting(order -> order.symbol)
                .containsExactly("EURUSD", "EURUSD");
        assertThat(orders).extracting(order -> order.direction)
                .containsExactly(BUY, SELL);
        assertThat(orders).extracting(order -> order.requestedAmount)
                .containsExactly(1_000, 2_000);
        assertThat(orders).extracting(order -> order.leftAmount)
                .containsExactly(0, 1_000);
        assertThat(orders).extracting(order -> order.amount)
                .containsExactly(1_000, 1_000);
        assertThat(orders).extracting(order -> order.limit)
                .containsExactly(0., 1.3235);
        assertThat(orders).extracting(order -> order.price)
                .containsExactly(1.3235, 1.3235);
        assertThat(orders).extracting(order -> order.status)
                .containsExactly(DONE, WORKING);
        assertThat(orders).extracting(order -> order.type)
                .containsExactly(MARKET, LIMIT);
    }

    @Test
    void should_update_order_on_market_order_placed() {

        // Given
        MarketOrderPlaced marketOrderPlaced = aMarketOrderPlaced()
                .withId(new UUID(0, 1))
                .withSide(BUY)
                .withQuantity(1_000)
                .withTime(LocalDateTime.of(2018, JULY, 1, 17, 1))
                .withSymbol("EURUSD")
                .withBroker("BROKER")
                .build();

        // When
        blotter.onMarketOrderPlaced(marketOrderPlaced);

        // Then
        verify(orderListener).onOrderUpdated(captor.capture());
        OrderUpdated orderUpdated = captor.getValue();
        assertThat(orderUpdated.id).isEqualTo(new UUID(0, 1));
        assertThat(orderUpdated.time).isEqualTo(LocalDateTime.of(2018, JULY, 1, 17, 1));
        assertThat(orderUpdated.symbol).isEqualTo("EURUSD");
        assertThat(orderUpdated.direction).isEqualTo(BUY);
        assertThat(orderUpdated.requestedAmount).isEqualTo(1_000);
        assertThat(orderUpdated.leftAmount).isEqualTo(1_000);
        assertThat(orderUpdated.amount).isEqualTo(0);
        assertThat(orderUpdated.limit).isEqualTo(0);
        assertThat(orderUpdated.price).isEqualTo(0);
        assertThat(orderUpdated.status).isEqualTo(SUBMITTING);
        assertThat(orderUpdated.type).isEqualTo(MARKET);
        assertThat(orderUpdated.broker).isEqualTo("BROKER");
    }

    @Test
    void should_compute_average_price() {

        // Given
        LimitOrderPlaced limitOrderPlaced1 = aLimitOrderPlaced()
                .withId(new UUID(0, 2))
                .withSide(BUY)
                .withQuantity(2_000)
                .build();

        LimitOrderPlaced limitOrderPlaced2 = aLimitOrderPlaced()
                .withId(new UUID(0, 3))
                .withSide(SELL)
                .withQuantity(2_000)
                .build();
        blotter.onLimitOrderPlaced(limitOrderPlaced1);
        blotter.onLimitOrderPlaced(limitOrderPlaced2);
        reset(orderListener);

        // When
        TradeExecuted tradeExecuted1 = aTradeExecuted()
                .withBuyingId(new UUID(0, 2))
                .withSellingId(new UUID(0, 3))
                .withQuantity(1_000)
                .withPrice(1.4)
                .build();
        blotter.onTradeExecuted(tradeExecuted1);

        // Then
        verify(orderListener, times(2)).onOrderUpdated(captor.capture());
        List<OrderUpdated> orders = captor.getAllValues();
        assertThat(orders).extracting(order -> order.id)
                .containsExactly(new UUID(0, 2), new UUID(0, 3));
        assertThat(orders).extracting(order -> order.price)
                .containsExactly(1.4, 1.4);

        // When
        reset(orderListener);
        captor = forClass(OrderUpdated.class);
        TradeExecuted tradeExecuted2 = aTradeExecuted()
                .withBuyingId(new UUID(0, 2))
                .withSellingId(new UUID(0, 3))
                .withQuantity(500)
                .withPrice(1.3)
                .build();
        blotter.onTradeExecuted(tradeExecuted2);

        // Then
        verify(orderListener, times(2)).onOrderUpdated(captor.capture());
        orders = captor.getAllValues();

        assertThat(orders).extracting(order -> order.id)
                .containsExactly(new UUID(0, 2), new UUID(0, 3));
        assertThat(orders).extracting(order -> order.price)
                .containsExactly(1.36667, 1.36667);
    }

    @Test
    void should_dispatch_order_updated() {

        // Given
        LimitOrderPlaced limitOrderPlaced1 = aLimitOrderPlaced()
                .withId(new UUID(0, 2))
                .withSide(BUY)
                .withQuantity(2_000)
                .build();

        LimitOrderPlaced limitOrderPlaced2 = aLimitOrderPlaced()
                .withId(new UUID(0, 3))
                .withSide(SELL)
                .withQuantity(2_000)
                .build();
        blotter.onLimitOrderPlaced(limitOrderPlaced1);
        blotter.onLimitOrderPlaced(limitOrderPlaced2);
        reset(orderListener);

        // When
        blotter.onSubscribe();

        // Then
        verify(orderListener, times(2)).onOrderUpdated(captor.capture());
        List<OrderUpdated> orders = captor.getAllValues();
        assertThat(orders).extracting(order -> order.id)
                .containsExactly(new UUID(0, 2), new UUID(0, 3));
    }

}