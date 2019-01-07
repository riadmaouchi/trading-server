package org.trading.market.domain;


import org.trading.market.command.LastTradePrice;
import org.trading.market.command.SubmitLimitOrder;
import org.trading.market.command.UpdatePrecision;
import org.trading.market.command.UpdateCurrencies;

public interface CommandListener {

    void onLastTradePrice(LastTradePrice lastTradePrice);

    void onSubmitLimitOrder(SubmitLimitOrder submitLimitOrder);

    void updatePrecision(UpdatePrecision updatePrecision);

    void updateCurrencies(UpdateCurrencies updateCurrencies);
}
