package org.trading.matching.engine.bdd.tag;

import com.tngtech.jgiven.annotation.IsTag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IsTag(type = "Feature",
        value = "Trade",
        description = "Trade is a basic economic concept involving the buying and selling of goods and services, with compensation paid by a buyer to a seller, or the exchange of goods or services between parties",
        color = "#F7BE81")
@Retention(RetentionPolicy.RUNTIME)
public @interface Trade {
}
