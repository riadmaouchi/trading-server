package org.trading.eventstore.domain;

public interface AggregateFactory<T extends Aggregate> {

    T create();
}
