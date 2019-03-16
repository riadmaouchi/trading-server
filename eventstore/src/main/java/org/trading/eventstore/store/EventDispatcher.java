package org.trading.eventstore.store;

import java.util.function.Consumer;

public class EventDispatcher<T> implements EventBus<T> {

    private final Consumer<T> consumer;

    public EventDispatcher(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void dispatch(T event) {
        consumer.accept(event);
    }

}
