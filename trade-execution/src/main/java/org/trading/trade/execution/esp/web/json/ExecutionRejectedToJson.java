package org.trading.trade.execution.esp.web.json;

import net.minidev.json.JSONObject;
import org.trading.trade.execution.esp.domain.ExecutionAccepted;
import org.trading.trade.execution.esp.domain.ExecutionRejected;

import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.*;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class ExecutionRejectedToJson {

    public JSONObject toJson(ExecutionRejected executionRejected) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tradeDate", executionRejected.tradeDate.format(ISO_DATE_TIME));
        jsonObject.put("id", executionRejected.id);
        jsonObject.put("symbol", executionRejected.symbol);
        jsonObject.put("direction", executionRejected.side.name());
        jsonObject.put("status", "REJECTED");
        jsonObject.put("price", executionRejected.price);
        jsonObject.put("quantity", executionRejected.quantity);
        jsonObject.put("broker", executionRejected.broker);
        return jsonObject;
    }
}
