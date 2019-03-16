package org.trading.matching.engine.api;

import org.trading.api.message.SubmitOrder;

public interface ExchangeRequestListener {

    void orderBookConfig(String symbol);

    void submitOrder(SubmitOrder submitOrder);
}
