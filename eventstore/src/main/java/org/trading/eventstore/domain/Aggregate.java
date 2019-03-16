package org.trading.eventstore.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Aggregate {
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    private UUID id;
    private int version;

    public void applyEvent(DomainEvent event) {
        apply(event);
        uncommittedEvents.add(event);
    }

    private void apply(DomainEvent event) {
        event.apply(this);
        ++version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    int getVersion() {
        return version;
    }

    protected long nextSequence() {
        return System.nanoTime();
    }

    public void fromEvents(List<DomainEvent> events) {
        events.forEach(this::apply);
    }

    public List<DomainEvent> getUncommittedEvents() {
        return List.copyOf(uncommittedEvents);
    }

    public void commitEvents() {
        uncommittedEvents.clear();
    }
}
