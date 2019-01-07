package org.trading.api.service;

import org.trading.api.command.SubmitOrder;

public interface OrderListener {

    void submitOrder(final SubmitOrder submitOrder);
}
