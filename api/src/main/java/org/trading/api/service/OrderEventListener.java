package org.trading.api.service;

import org.trading.api.event.LimitOrderAccepted;
import org.trading.api.event.MarketOrderAccepted;
import org.trading.api.event.MarketOrderRejected;
import org.trading.api.event.TradeExecuted;

public interface OrderEventListener {

    void onMarketOrderPlaced(final MarketOrderAccepted marketOrderPlaced);

    void onLimitOrderPlaced(final LimitOrderAccepted limitOrderAccepted);

    void onTradeExecuted(final TradeExecuted tradeExecuted);

    void onMarketOrderRejected(final MarketOrderRejected marketOrderRejected);

}
