package org.trading.market.domain;

import org.trading.market.event.OrderSubmitted;

public interface OrderEventListener {

    void onOrderSubmitted(OrderSubmitted orderSubmitted);
}
