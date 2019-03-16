package org.trading.eventstore.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AggregateTest {

    @Test
    void should_increment_aggregate_version() {

        // Given
        DomainEvent domainEvent1 = new DomainEvent(new UUID(0, 1), 1L) {
            @Override
            public void apply(Aggregate aggregate) {

            }
        };
        DomainEvent domainEvent2 = new DomainEvent(new UUID(0, 1), 2L) {
            @Override
            public void apply(Aggregate aggregate) {

            }
        };
        Aggregate aggregate = new Aggregate() {
        };

        // When

        aggregate.applyEvent(domainEvent1);

        // Then
        assertThat(aggregate.getVersion()).isEqualTo(1L);

        // When

        aggregate.applyEvent(domainEvent2);

        // Then
        assertThat(aggregate.getVersion()).isEqualTo(2L);
    }

    @Test
    void should_apply_event() {

        // Given
        DummyAggregate aggregate = new DummyAggregate();
        DummyEvent1 dummyEvent1 = new DummyEvent1(
                new UUID(0, 1),
                1L,
                "anAttribute"
        );

        // When
        aggregate.applyEvent(dummyEvent1);

        // Then
        assertThat(aggregate.attribute1).isEqualTo("anAttribute");
        assertThat(aggregate.getUncommittedEvents()).containsExactly(dummyEvent1);
    }

    @Test
    void should_apply_events_from_history() {

        // Given
        DummyAggregate aggregate = new DummyAggregate();
        DummyEvent1 dummyEvent1 = new DummyEvent1(
                new UUID(0, 1),
                1L,
                "anAttribute"
        );
        DummyEvent2 dummyEvent2 = new DummyEvent2(
                new UUID(0, 1),
                1L,
                "aSecondAttribute"
        );

        // When
        aggregate.fromEvents(List.of(dummyEvent1, dummyEvent2));

        // Then
        assertThat(aggregate.attribute1).isEqualTo("anAttribute");
        assertThat(aggregate.attribute2).isEqualTo("aSecondAttribute");
        assertThat(aggregate.getUncommittedEvents()).isEmpty();
    }

    @Test
    void should_commit_events() {

        // Given
        DummyAggregate aggregate = new DummyAggregate();
        DummyEvent1 dummyEvent1 = new DummyEvent1(
                new UUID(0, 1),
                1L,
                "anAttribute"
        );
        aggregate.applyEvent(dummyEvent1);

        // When
        aggregate.commitEvents();

        // Then
        assertThat(aggregate.getUncommittedEvents()).isEmpty();
    }


    private class DummyAggregate extends Aggregate {
        private String attribute1;
        private String attribute2;
    }

    private class DummyEvent1 extends DomainEvent<DummyAggregate> {
        private final String attribute;

        DummyEvent1(UUID id, long sequenceNumber, String attribute) {
            super(id, sequenceNumber);
            this.attribute = attribute;
        }

        @Override
        public void apply(DummyAggregate dummyAggregate) {
            dummyAggregate.attribute1 = attribute;
        }
    }

    private class DummyEvent2 extends DomainEvent<DummyAggregate> {
        private final String attribute;

        DummyEvent2(UUID id, long sequenceNumber, String attribute) {
            super(id, sequenceNumber);
            this.attribute = attribute;
        }

        @Override
        public void apply(DummyAggregate dummyAggregate) {
            dummyAggregate.attribute2 = attribute;
        }
    }


}