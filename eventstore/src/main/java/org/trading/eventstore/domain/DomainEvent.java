package org.trading.eventstore.domain;

import java.util.UUID;

public abstract class DomainEvent<T extends Aggregate> implements Event {
    private final UUID id;
    private final long sequenceNumber;

    protected DomainEvent(UUID id, long sequenceNumber) {
        this.id = id;
        this.sequenceNumber = sequenceNumber;
    }

    public abstract void apply(T t);

    @Override
    public UUID getAggregateId() {
        return id;
    }

    @Override
    public long getSequence() {
        return sequenceNumber;
    }
}
