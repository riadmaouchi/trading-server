package org.trading.trade.execution.esp.web.json;

import net.minidev.json.JSONObject;
import org.trading.trade.execution.esp.domain.ExecutionAccepted;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class ExecutionAcceptedToJson {

    public JSONObject toJson(ExecutionAccepted executionAccepted) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tradeDate", executionAccepted.tradeDate.format(ISO_DATE_TIME));
        jsonObject.put("id", executionAccepted.id);
        jsonObject.put("symbol", executionAccepted.symbol);
        jsonObject.put("direction", executionAccepted.side.name());
        jsonObject.put("status", "ACCEPTED");
        jsonObject.put("price", executionAccepted.price);
        jsonObject.put("quantity", executionAccepted.quantity);
        jsonObject.put("broker", executionAccepted.broker);
        jsonObject.put("reason", "");
        return jsonObject;
    }
}
