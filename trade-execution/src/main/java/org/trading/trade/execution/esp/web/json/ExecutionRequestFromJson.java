package org.trading.trade.execution.esp.web.json;

import net.minidev.json.JSONObject;
import org.trading.api.message.Side;
import org.trading.trade.execution.esp.domain.ExecutionRequest;

public class ExecutionRequestFromJson {

    public ExecutionRequest toJson(JSONObject jsonObject) {
       return new ExecutionRequest(
         jsonObject.getAsString("broker"),
         jsonObject.getAsNumber("quantity").intValue(),
               Side.valueOf(jsonObject.getAsString("side").toUpperCase()),
               jsonObject.getAsString("symbol"),
               jsonObject.getAsNumber("price").doubleValue()
       );
    }
}
