package org.trading.trade.execution.order.domain;

import org.trading.trade.execution.order.event.OrderUpdated;

public interface OrderListener {

    void onOrderUpdated(OrderUpdated orderUpdated);
}
