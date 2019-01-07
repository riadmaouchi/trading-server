package org.trading.trade.execution.order.domain;

import org.trading.api.command.Side;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.api.event.MarketOrderPlaced;
import org.trading.api.event.TradeExecuted;
import org.trading.api.service.OrderEventListener;
import org.trading.trade.execution.order.event.LastTradeExecuted;
import org.trading.trade.execution.order.event.OrderLevelUpdated;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class OrderBook implements OrderEventListener {
    private final Map<String, CurrencyPair> availableCurrencyPairs = new HashMap<>();
    private final OrderLevelListener orderLevelListener;

    public OrderBook(OrderLevelListener orderLevelListener) {
        this.orderLevelListener = orderLevelListener;
    }

    public void subscribe() {
        availableCurrencyPairs.forEach((symbol, currencyPair) -> {
            currencyPair.getBuyOrders().forEach((price, event) -> orderLevelListener.onOrderLevelUpdated(event));
            currencyPair.getSellOrders().forEach((price, event) -> orderLevelListener.onOrderLevelUpdated(event));
            currencyPair.getLastTrade().ifPresent(orderLevelListener::onLastTradeExecuted);
        });
    }

    public void updateIndicators() {
        availableCurrencyPairs.forEach((symbol, currencyPair) -> currencyPair.updateOHLC());
    }

    @Override
    public void onMarketOrderPlaced(MarketOrderPlaced marketOrderPlaced) {
        // nop
    }

    @Override
    public void onLimitOrderPlaced(LimitOrderPlaced limitOrderPlaced) {
        OrderLevelUpdated order = new OrderLevelUpdated(
                limitOrderPlaced.symbol,
                limitOrderPlaced.side,
                limitOrderPlaced.quantity,
                limitOrderPlaced.price
        );

        final CurrencyPair currencyPair = availableCurrencyPairs.computeIfAbsent(
                order.symbol,
                symbol -> new CurrencyPair(order.price)
        );
        orderLevelListener.onOrderLevelUpdated(currencyPair.placeOrder(order));
    }

    @Override
    public void onTradeExecuted(TradeExecuted tradeExecuted) {
        OrderLevelUpdated buyOrder = new OrderLevelUpdated(
                tradeExecuted.symbol,
                Side.BUY,
                tradeExecuted.quantity,
                tradeExecuted.buyingLimit
        );

        OrderLevelUpdated sellOrder = new OrderLevelUpdated(
                tradeExecuted.symbol,
                Side.SELL,
                tradeExecuted.quantity,
                tradeExecuted.sellingLimit
        );

        availableCurrencyPairs.computeIfPresent(tradeExecuted.symbol, (symbol, currencyPair) -> {
                    orderLevelListener.onOrderLevelUpdated(currencyPair.executeOrder(buyOrder));
                    orderLevelListener.onOrderLevelUpdated(currencyPair.executeOrder(sellOrder));
                    return currencyPair;
                }
        );

        LastTradeExecuted lastTradeExecuted = new LastTradeExecuted(
                tradeExecuted.symbol,
                tradeExecuted.price,
                tradeExecuted.quantity,
                tradeExecuted.time
        );
        availableCurrencyPairs.computeIfPresent(lastTradeExecuted.symbol, (symbol, currencyPair) -> {
            orderLevelListener.onLastTradeExecuted(currencyPair.update(lastTradeExecuted));
            return currencyPair;
        });


    }
}
