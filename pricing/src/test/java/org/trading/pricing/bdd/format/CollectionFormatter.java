package org.trading.pricing.bdd.format;

import com.tngtech.jgiven.format.ArgumentFormatter;

import java.util.Collection;

import static java.util.stream.Collectors.joining;

public class CollectionFormatter implements ArgumentFormatter<Collection<Integer>> {

    @Override
    public String format(Collection<Integer> argumentToFormat, String... formatterArguments) {
        return argumentToFormat.stream()
                .map(value -> Integer.toString(value))
                .collect(joining(", "));
    }
}
