package org.trading.matching.engine.bdd.tag;

import com.tngtech.jgiven.annotation.IsTag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IsTag(type = "Feature", value = "Order", color = "#D87296")
@Retention( RetentionPolicy.RUNTIME )
public @interface Order {
}
