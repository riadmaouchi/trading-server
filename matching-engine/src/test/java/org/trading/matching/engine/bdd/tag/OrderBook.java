package org.trading.matching.engine.bdd.tag;

import com.tngtech.jgiven.annotation.IsTag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IsTag(type = "Feature",
        value = "Order Book",
        description = "An order book is an electronic list of buy and sell orders for a specific security or financial instrument, organized by price level",
        color = "#9D99E5")
@Retention(RetentionPolicy.RUNTIME)
public @interface OrderBook {
}
