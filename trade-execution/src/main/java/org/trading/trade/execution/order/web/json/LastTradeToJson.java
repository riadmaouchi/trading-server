package org.trading.trade.execution.order.web.json;

import net.minidev.json.JSONObject;
import org.trading.api.event.TradeExecuted;
import org.trading.trade.execution.order.event.LastTradeExecuted;

public class LastTradeToJson {

    public JSONObject toJson(LastTradeExecuted lastTradeExecuted) {
        JSONObject json = new JSONObject();
        json.put("symbol", lastTradeExecuted.symbol);
        json.put("lastPrice", lastTradeExecuted.lastPrice);
        json.put("lastQuantity", lastTradeExecuted.lastQuantity);
        json.put("time", lastTradeExecuted.time.toString());
        json.put("open", lastTradeExecuted.open);
        json.put("high", lastTradeExecuted.high);
        json.put("low", lastTradeExecuted.low);
        json.put("close", lastTradeExecuted.close);
        return json;
    }
}
