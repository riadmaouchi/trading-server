package org.trading.pricing.web.json;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.trading.pricing.domain.Price;
import org.trading.pricing.domain.Prices;

import java.util.List;
import java.util.function.Predicate;

import static java.lang.Double.isNaN;
import static java.util.stream.Collectors.toCollection;

public final class PricesToJson {

    public final JSONObject toJson(Prices price) {
        JSONObject json = new JSONObject();
        json.put("symbol", price.symbol);
        json.put("time", price.time.toString());
        json.put("mid", price.midMarketPrice);
        json.put("asks", toJson(price.asks));
        json.put("bids", toJson(price.bids));
        return json;
    }

    private JSONArray toJson(List<Price> asks) {
        return asks.stream()
                .filter(((Predicate<Price>) p -> isNaN(p.price)).negate())
                .map(p -> {
                    JSONObject jsonPrice = new JSONObject();
                    jsonPrice.put("quantity", p.liquidity);
                    jsonPrice.put("price", p.price);
                    return jsonPrice;
                }).collect(toCollection(JSONArray::new));
    }
}
