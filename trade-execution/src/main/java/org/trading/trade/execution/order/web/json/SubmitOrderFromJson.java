package org.trading.trade.execution.order.web.json;

import net.minidev.json.JSONObject;
import org.trading.MessageProvider.OrderType;
import org.trading.MessageProvider.Side;
import org.trading.MessageProvider.SubmitOrder;

import static java.lang.Double.NaN;


public class SubmitOrderFromJson {

    public SubmitOrder fromJson(JSONObject jsonObject) {
        final OrderType orderType = OrderType.valueOf(jsonObject.getAsString("type").toUpperCase());
        return SubmitOrder.newBuilder()
                .setOrderType(orderType)
                .setSymbol(jsonObject.getAsString("symbol"))
                .setBroker(jsonObject.getAsString("broker"))
                .setAmount(jsonObject.getAsNumber("amount").intValue())
                .setSide(Side.valueOf(jsonObject.getAsString("side").toUpperCase()))
                .setPrice(orderType == OrderType.LIMIT ? jsonObject.getAsNumber("price").doubleValue() : NaN)
                .build();
    }
}