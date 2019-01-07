package org.trading.api.service;

import org.trading.api.event.LimitOrderPlaced;
import org.trading.api.event.MarketOrderPlaced;
import org.trading.api.event.TradeExecuted;

public interface OrderEventListener {

    void onMarketOrderPlaced(final MarketOrderPlaced marketOrderPlaced);

    void onLimitOrderPlaced(final LimitOrderPlaced limitOrderPlaced);

    void onTradeExecuted(final TradeExecuted tradeExecuted);

}
