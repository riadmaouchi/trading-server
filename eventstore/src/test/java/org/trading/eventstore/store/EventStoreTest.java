package org.trading.eventstore.store;

import com.google.common.collect.Lists;
import org.apache.avro.Schema;
import org.apache.avro.SchemaNormalization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trading.eventstore.domain.Event;
import org.trading.eventstore.serializer.Serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventStoreTest {

    private InMemoryStore store;

    @BeforeEach
    void before() {
        Collection<Serializer> serializers = Lists.newArrayList();
        serializers.add(new SimpleSerializer());
        store = new InMemoryStore(serializers);
    }

    @Test
    public void retrieve_stored_events() {
        // given
        UUID id = UUID.randomUUID();
        // when
        store.store(new SimpleEvent(id, 0));
        // then
        List<Event> events = store.loadEventStream(id);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getAggregateId()).isEqualTo(id);
        assertThat(events.get(0).getSequence()).isEqualTo(0);
    }

    public static class InMemoryStore extends EventStore {

        private Map<Key, byte[]> events = new HashMap<>();
        private Map<Long, Schema> schemas = new HashMap<>();

        public InMemoryStore(Collection<Serializer> serializers) {
            super(serializers);
            for (Serializer serializer : serializers) {
                Schema schema = serializer.getSchema();
                long fingerprint = SchemaNormalization.parsingFingerprint64(schema);
                schemas.put(fingerprint, schema);
            }
        }

        @Override
        protected void store(Key key, byte[] data) {
            events.put(key, data);
        }

        @Override
        protected Schema loadSchema(long fingerprint) {
            return schemas.get(fingerprint);
        }

        @Override
        public List<Event> loadEventStream(UUID aggregateId) {
            Set<Map.Entry<Key, byte[]>> entries = events.entrySet();
            List<Event> events = new ArrayList<>();
            for (Map.Entry<Key, byte[]> entry : entries) {
                events.add(deserializeEvent(entry.getValue(), entry.getKey().schemaFingerprint));
            }
            return events;
        }

        @Override
        public void close() {
            // not needed
        }
    }
}