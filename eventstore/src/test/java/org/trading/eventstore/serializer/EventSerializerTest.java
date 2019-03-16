package org.trading.eventstore.serializer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.eventstore.domain.Event;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventSerializerTest {

    private EventSerializer<DummyEvent> eventSerializer;

    @BeforeEach
    void before() {
        eventSerializer = new EventSerializer<>() {
            @Override
            public Class getEventClass() {
                return DummyEvent.class;
            }

            @Override
            public Schema getSchema() {
                return getEventSchema("DummyEvent")
                        .name("symbol").type().stringType().noDefault()
                        .endRecord();
            }

            @Override
            public DummyEvent deserialize(UUID id, long sequence, GenericRecord record) {
                return new DummyEvent(id, sequence, record.get("symbol").toString());
            }

            @Override
            public void serialize(GenericData.Record record, DummyEvent event) {
                record.put("symbol", event.symbol);
            }
        };
    }

    @Test
    void should_serialize_event() {
        // Given
        DummyEvent dummyEvent = new DummyEvent(new UUID(0, 1), 1L, "Symbol");

        // When
        byte[] bytes = eventSerializer.serialize(dummyEvent);

        // Then
        DummyEvent deserialize = eventSerializer.deserialize(bytes, eventSerializer.getSchema());
        assertThat(deserialize).isEqualToComparingFieldByFieldRecursively(dummyEvent);
    }

    public static class DummyEvent implements Event {

        final UUID id;
        final long sequence;
        final String symbol;

        private DummyEvent(UUID id, long sequence, String symbol) {
            this.id = id;
            this.sequence = sequence;
            this.symbol = symbol;
        }

        @Override
        public UUID getAggregateId() {
            return id;
        }

        @Override
        public long getSequence() {
            return sequence;
        }
    }

}