package org.trading.eventstore.store;

import org.trading.eventstore.domain.Event;

import java.util.UUID;

public class SimpleEvent implements Event {

    private final UUID id;

    private final long sequenceNumber;

    public SimpleEvent(UUID id,long sequenceNumber) {
        this.id = id;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public UUID getAggregateId() {
        return id;
    }

    @Override
    public long getSequence() {
        return sequenceNumber;
    }
}
