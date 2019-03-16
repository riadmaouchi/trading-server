package org.trading.trade.execution.order.domain;

import org.trading.api.event.LimitOrderAccepted;
import org.trading.api.event.MarketOrderAccepted;
import org.trading.api.event.MarketOrderRejected;
import org.trading.api.event.TradeExecuted;
import org.trading.api.message.OrderType.OrderTypeVisitor;
import org.trading.api.message.Side;
import org.trading.api.service.OrderEventListener;
import org.trading.trade.execution.order.event.LastTradeExecuted;
import org.trading.trade.execution.order.event.OrderLevelUpdated;

import java.util.HashMap;
import java.util.Map;

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
    public void onMarketOrderPlaced(MarketOrderAccepted marketOrderPlaced) {
        // nop
    }

    @Override
    public void onLimitOrderPlaced(LimitOrderAccepted limitOrderAccepted) {
        OrderLevelUpdated order = new OrderLevelUpdated(
                limitOrderAccepted.symbol,
                limitOrderAccepted.side,
                limitOrderAccepted.quantity,
                limitOrderAccepted.price
        );

        final CurrencyPair currencyPair = availableCurrencyPairs.computeIfAbsent(
                order.symbol,
                symbol -> new CurrencyPair(order.price)
        );
        orderLevelListener.onOrderLevelUpdated(currencyPair.placeOrder(order));
    }

    @Override
    public void onTradeExecuted(TradeExecuted tradeExecuted) {

        tradeExecuted.buyingOrderType.accept(new OrderTypeVisitor<Void>() {
            @Override
            public Void visitMarket() {
                // nop
                return null;
            }

            @Override
            public Void visitLimit() {
                OrderLevelUpdated buyOrder = new OrderLevelUpdated(
                        tradeExecuted.symbol,
                        Side.BUY,
                        tradeExecuted.quantity,
                        tradeExecuted.buyingLimit
                );

                availableCurrencyPairs.computeIfPresent(tradeExecuted.symbol, (symbol, currencyPair) -> {
                            orderLevelListener.onOrderLevelUpdated(currencyPair.executeOrder(buyOrder));
                            return currencyPair;
                        }
                );


                return null;
            }
        });

        tradeExecuted.sellingOrderType.accept(new OrderTypeVisitor<Void>() {
            @Override
            public Void visitMarket() {
                // nop
                return null;
            }

            @Override
            public Void visitLimit() {
                OrderLevelUpdated sellOrder = new OrderLevelUpdated(
                        tradeExecuted.symbol,
                        Side.SELL,
                        tradeExecuted.quantity,
                        tradeExecuted.sellingLimit
                );
                availableCurrencyPairs.computeIfPresent(tradeExecuted.symbol, (symbol, currencyPair) -> {
                            orderLevelListener.onOrderLevelUpdated(currencyPair.executeOrder(sellOrder));
                            return currencyPair;
                        }
                );

                return null;
            }
        });


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

    @Override
    public void onMarketOrderRejected(MarketOrderRejected marketOrderRejected) {
        // nop
    }
}
