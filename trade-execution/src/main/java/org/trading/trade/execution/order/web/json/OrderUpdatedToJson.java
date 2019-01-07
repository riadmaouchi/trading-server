package org.trading.trade.execution.order.web.json;

import net.minidev.json.JSONObject;
import org.trading.trade.execution.order.event.OrderUpdated;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class OrderUpdatedToJson {

    public JSONObject toJson(OrderUpdated orderUpdated) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", orderUpdated.id.toString());
        jsonObject.put("symbol", orderUpdated.symbol);
        jsonObject.put("direction", orderUpdated.direction.name());
        jsonObject.put("broker", orderUpdated.broker);
        jsonObject.put("time", orderUpdated.time.format(ISO_DATE_TIME));
        jsonObject.put("requestedAmount", orderUpdated.requestedAmount);
        jsonObject.put("leftAmount", orderUpdated.leftAmount);
        jsonObject.put("amount", orderUpdated.amount);
        jsonObject.put("type", orderUpdated.type.name());
        jsonObject.put("limit", orderUpdated.limit);
        jsonObject.put("price", orderUpdated.price);
        jsonObject.put("status", orderUpdated.status.name());
        return jsonObject;
    }
}
