package org.trading.trade.execution.order.domain;

import org.trading.trade.execution.order.event.LastTradeExecuted;
import org.trading.trade.execution.order.event.OrderLevelUpdated;

public interface OrderLevelListener {

    void onOrderLevelUpdated(OrderLevelUpdated orderLevelUpdated);

    void onLastTradeExecuted(LastTradeExecuted lastTradeExecuted);
}
