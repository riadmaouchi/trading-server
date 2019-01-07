package org.trading.matching.engine.bdd.tag;

import com.tngtech.jgiven.annotation.IsTag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Order
@IsTag(value = "Limit Order",
        description = "Limit order is a take-profit order placed with a bank or brokerage to buy or sell a set amount of a financial instrument at a specified price or better",
        color = "#9BD55D")
@Retention(RetentionPolicy.RUNTIME)
public @interface LimitOrder {
}
