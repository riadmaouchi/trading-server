package org.trading.matching.engine.bdd.tag;

import com.tngtech.jgiven.annotation.IsTag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IsTag(type = "Feature",
        value = "Matching Orders",
        description = "The process for executing securities trades by pairing buy orders with sell orders. Matching orders utilize algorithms which determine how orders are matched and in what order they are filled",
        color = "#A9D0F5")
@Retention(RetentionPolicy.RUNTIME)
public @interface MatchingOrders {
}
