package org.trading.trade.execution.order.domain;

import com.tngtech.junit.dataprovider.DataProvider;
import com.tngtech.junit.dataprovider.DataProviderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.trading.api.command.Side;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.api.event.TradeExecuted;
import org.trading.trade.execution.order.event.LastTradeExecuted;
import org.trading.trade.execution.order.event.OrderLevelUpdated;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.Month.JULY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.trading.api.LimitOrderPlacedBuilder.aLimitOrderPlaced;
import static org.trading.api.command.Side.BUY;
import static org.trading.api.command.Side.SELL;
import static org.trading.trade.execution.TradeExecutedBuilder.aTradeExecuted;

@ExtendWith(DataProviderExtension.class)
class OrderBookTest {

    private OrderLevelListener orderLevelListener;
    private OrderBook orderBook;
    private ArgumentCaptor<OrderLevelUpdated> captor;

    @BeforeEach
    void before() {
        orderLevelListener = mock(OrderLevelListener.class);
        orderBook = new OrderBook(orderLevelListener);
        captor = forClass(OrderLevelUpdated.class);
    }

    @DataProvider({
            "BUY",
            "SELL",
    })
    @TestTemplate
    void should_update_order_level_on_limit_order_placed(Side side) {
        // Given
        LimitOrderPlaced limitOrderPlaced = aLimitOrderPlaced()
                .withSymbol("EURUSD")
                .withSide(side)
                .withPrice(1.3235)
                .withQuantity(1_000)
                .build();

        // When
        orderBook.onLimitOrderPlaced(limitOrderPlaced);

        // Then
        verify(orderLevelListener).onOrderLevelUpdated(captor.capture());
        OrderLevelUpdated orderLevelUpdated = captor.getValue();
        assertThat(orderLevelUpdated.symbol).isEqualTo("EURUSD");
        assertThat(orderLevelUpdated.side).isEqualTo(side);
        assertThat(orderLevelUpdated.price).isEqualTo(1.3235);
        assertThat(orderLevelUpdated.quantity).isEqualTo(1_000);
    }

    @DataProvider({
            "BUY",
            "SELL",
    })
    @TestTemplate
    void should_increment_order_level_quantity_on_limit_order_placed(Side side) {
        // Given
        LimitOrderPlaced firstLimitOrderPlaced = aLimitOrderPlaced()
                .withSymbol("EURUSD")
                .withSide(side)
                .withPrice(1.3235)
                .withQuantity(1_000)
                .build();
        orderBook.onLimitOrderPlaced(firstLimitOrderPlaced);
        reset(orderLevelListener);

        LimitOrderPlaced secondLimitOrderPlaced = aLimitOrderPlaced()
                .withSymbol("EURUSD")
                .withSide(side)
                .withPrice(1.3235)
                .withQuantity(1_000)
                .build();

        // When
        orderBook.onLimitOrderPlaced(secondLimitOrderPlaced);

        // Then
        verify(orderLevelListener).onOrderLevelUpdated(captor.capture());
        OrderLevelUpdated orderLevelUpdated = captor.getValue();
        assertThat(orderLevelUpdated.symbol).isEqualTo("EURUSD");
        assertThat(orderLevelUpdated.side).isEqualTo(side);
        assertThat(orderLevelUpdated.price).isEqualTo(1.3235);
        assertThat(orderLevelUpdated.quantity).isEqualTo(2_000);
    }

    @Test
    void should_update_order_levels_on_trade_executed() {
        // Given
        LimitOrderPlaced firstLimitOrderPlaced = aLimitOrderPlaced()
                .withSymbol("EURUSD")
                .withSide(BUY)
                .withPrice(1.3235)
                .withQuantity(2_000)
                .build();
        orderBook.onLimitOrderPlaced(firstLimitOrderPlaced);

        LimitOrderPlaced secondLimitOrderPlaced = aLimitOrderPlaced()
                .withSymbol("EURUSD")
                .withSide(SELL)
                .withPrice(1.3235)
                .withQuantity(1_000)
                .build();
        orderBook.onLimitOrderPlaced(secondLimitOrderPlaced);
        reset(orderLevelListener);

        TradeExecuted tradeExecuted = aTradeExecuted()
                .withSymbol("EURUSD")
                .withBuyingLimit(1.3235)
                .withSellingLimit(1.3235)
                .withQuantity(800)
                .withPrice(1.3235)
                .build();

        // When
        orderBook.onTradeExecuted(tradeExecuted);


        // Then
        verify(orderLevelListener, times(2)).onOrderLevelUpdated(captor.capture());
        List<OrderLevelUpdated> orders = captor.getAllValues();
        assertThat(orders).extracting(order -> order.symbol).containsExactly("EURUSD", "EURUSD");
        assertThat(orders).extracting(order -> order.side).containsExactly(BUY, SELL);
        assertThat(orders).extracting(order -> order.price).containsExactly(1.3235, 1.3235);
        assertThat(orders).extracting(order -> order.quantity).containsExactly(1200, 200);

    }

    @Test
    void should_update_last_trade_price_on_trade_executed() {
        // Given
        LimitOrderPlaced firstLimitOrderPlaced = aLimitOrderPlaced()
                .withSymbol("EURUSD")
                .withSide(BUY)
                .withPrice(1.3235)
                .withQuantity(2_000)
                .build();
        orderBook.onLimitOrderPlaced(firstLimitOrderPlaced);

        LimitOrderPlaced secondLimitOrderPlaced = aLimitOrderPlaced()
                .withSymbol("EURUSD")
                .withSide(SELL)
                .withPrice(1.3235)
                .withQuantity(1_000)
                .build();
        orderBook.onLimitOrderPlaced(secondLimitOrderPlaced);
        reset(orderLevelListener);

        TradeExecuted tradeExecuted = aTradeExecuted()
                .withSymbol("EURUSD")
                .withTime(LocalDateTime.of(2018, JULY, 1, 17, 1))
                .withQuantity(800)
                .withPrice(1.3235)
                .build();


        // When
        orderBook.onTradeExecuted(tradeExecuted);

        // Then
        ArgumentCaptor<LastTradeExecuted> captor = forClass(LastTradeExecuted.class);
        verify(orderLevelListener).onLastTradeExecuted(captor.capture());
        LastTradeExecuted lastTradeExecuted = captor.getValue();
        assertThat(lastTradeExecuted.symbol).isEqualTo("EURUSD");
        assertThat(lastTradeExecuted.lastPrice).isEqualTo(1.3235);
        assertThat(lastTradeExecuted.lastQuantity).isEqualTo(800);
        assertThat(lastTradeExecuted.time).isEqualTo(LocalDateTime.of(2018, JULY, 1, 17, 1));
        assertThat(lastTradeExecuted.open).isEqualTo(1.3235);
        assertThat(lastTradeExecuted.high).isEqualTo(1.3235);
        assertThat(lastTradeExecuted.low).isEqualTo(1.3235);
        assertThat(lastTradeExecuted.close).isEqualTo(1.3235);
    }

    @Test
    void should_update_open_close() {
        // Given
        LimitOrderPlaced firstLimitOrderPlaced = aLimitOrderPlaced()
                .withSymbol("EURUSD")
                .withPrice(1.3235)
                .build();
        orderBook.onLimitOrderPlaced(firstLimitOrderPlaced);

        reset(orderLevelListener);

        TradeExecuted tradeExecuted = aTradeExecuted()
                .withSymbol("EURUSD")
                .withPrice(1.1278)
                .build();
        orderBook.updateIndicators();

        // When
        orderBook.onTradeExecuted(tradeExecuted);

        // Then
        ArgumentCaptor<LastTradeExecuted> captor = forClass(LastTradeExecuted.class);
        verify(orderLevelListener).onLastTradeExecuted(captor.capture());
        LastTradeExecuted lastTradeExecuted = captor.getValue();
        assertThat(lastTradeExecuted.symbol).isEqualTo("EURUSD");
        assertThat(lastTradeExecuted.open).isEqualTo(1.3235);
        assertThat(lastTradeExecuted.high).isEqualTo(1.3235);
        assertThat(lastTradeExecuted.low).isEqualTo(1.1278);
        assertThat(lastTradeExecuted.close).isEqualTo(1.1278);
    }
}