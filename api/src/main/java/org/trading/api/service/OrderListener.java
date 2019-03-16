package org.trading.api.service;

import org.trading.api.message.SubmitOrder;

public interface OrderListener {

    void submitOrder(final SubmitOrder submitOrder);
}
