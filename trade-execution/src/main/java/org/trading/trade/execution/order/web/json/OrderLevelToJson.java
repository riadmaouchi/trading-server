package org.trading.trade.execution.order.web.json;

import net.minidev.json.JSONObject;
import org.trading.trade.execution.order.event.OrderLevelUpdated;

public class OrderLevelToJson {

    public JSONObject toJson(OrderLevelUpdated orderLevelUpdated) {
        JSONObject json = new JSONObject();
        json.put("symbol", orderLevelUpdated.symbol);
        json.put("side", orderLevelUpdated.side);
        json.put("size", orderLevelUpdated.quantity);
        json.put("price", orderLevelUpdated.price);
        return json;
    }
}
