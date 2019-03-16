package org.trading.matching.engine.api;

import org.trading.matching.engine.domain.OrderBook;

public interface ExchangeResponseListener {

    void onOrderBookCreated(OrderBook.OrderBookCreated orderBookCreated);

    void onMarketOrderAccepted(OrderBook.MarketOrderAccepted marketOrderAccepted);

    void onMarketOrderRejected(OrderBook.MarketOrderRejected marketOrderRejected);

    void onLimitOrderAccepted(OrderBook.LimitOrderAccepted limitOrderAccepted);

    void onBuyLimitOrderPlaced(OrderBook.BuyLimitOrderPlaced buyLimitOrderPlaced);

    void onSellLimitOrderPlaced(OrderBook.SellLimitOrderPlaced sellLimitOrderPlaced);

    void onTradeExecuted(OrderBook.TradeExecuted tradeExecuted);
}
