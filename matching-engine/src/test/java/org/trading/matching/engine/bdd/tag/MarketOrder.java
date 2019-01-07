package org.trading.matching.engine.bdd.tag;

import com.tngtech.jgiven.annotation.IsTag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Order
@IsTag(value = "Market Order",
        description = "An investor makes a market order through a broker or brokerage service to buy or sell an investment immediately at the best available current price",
        color = "#CE71D9")
@Retention(RetentionPolicy.RUNTIME)
public @interface MarketOrder {
}
