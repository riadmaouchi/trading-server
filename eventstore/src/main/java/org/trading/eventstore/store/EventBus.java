package org.trading.eventstore.store;

public interface EventBus<T> {

    void dispatch(T event);
}
