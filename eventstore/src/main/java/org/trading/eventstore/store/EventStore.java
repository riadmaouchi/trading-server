package org.trading.eventstore.store;

import com.google.common.collect.ImmutableMap;
import org.apache.avro.Schema;
import org.apache.avro.SchemaNormalization;
import org.trading.eventstore.domain.Event;
import org.trading.eventstore.serializer.Serializer;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.requireNonNull;


public abstract class EventStore<T extends Event> implements Store<T> {

    private final Map<Class, Serializer> serializersByClass;
    private final Map<Class, Long> fingerprintByClass;
    private final Map<String, Serializer> serializersByRecordName;
    private final Map<Long, Schema> schemaByFingerprint = new HashMap<>();

    public EventStore(Collection<Serializer> serializers) {

        Map<Class, Serializer> serializerMap = new HashMap<>();
        Map<String, Serializer> serializerByRecordMap = new HashMap<>();
        Map<Class, Long> fingerprintMap = new HashMap<>();
        for (Serializer serializer : serializers) {
            serializerMap.put(serializer.getEventClass(), serializer);
            Schema schema = serializer.getSchema();
            serializerByRecordMap.put(schema.getFullName(), serializer);
            long fingerprint = SchemaNormalization.parsingFingerprint64(schema);
            fingerprintMap.put(serializer.getEventClass(), fingerprint);
        }

        serializersByClass
                = new ImmutableMap.Builder<Class, Serializer>().putAll(serializerMap).build();
        serializersByRecordName
                = new ImmutableMap.Builder<String, Serializer>().putAll(serializerByRecordMap).build();
        fingerprintByClass
                = new ImmutableMap.Builder<Class, Long>().putAll(fingerprintMap).build();

    }

    @Override
    public final void store(T event) {
        requireNonNull(event, "event");
        Serializer serializer = serializersByClass.get(event.getClass());
        byte[] bytes = serializer.serialize(event);

        store(new Key(event.getAggregateId(), event.getSequence(), fingerprintByClass.get(event.getClass())), bytes);
    }

    protected abstract void store(Key key, byte[] data);

    protected T deserializeEvent(byte[] data, long fingerprint) {
        Schema schema = schemaByFingerprint.get(fingerprint);
        if (schema == null) {
            schema = loadSchema(fingerprint);
            schemaByFingerprint.put(fingerprint, schema);
        }
        Serializer<T> serializer = serializersByRecordName.get(schema.getFullName());
        return serializer.deserialize(data, schema);
    }

    protected abstract Schema loadSchema(long fingerprint);

    public static class Key {

        public final UUID id;

        public final long sequence;
        public final long schemaFingerprint;

        public Key(UUID id) {
            this.id = id;
            this.sequence = 0L;
            this.schemaFingerprint = 0L;
        }

        Key(UUID id, long sequence, long schemaFingerprint) {
            this.id = id;
            this.sequence = sequence;
            this.schemaFingerprint = schemaFingerprint;
        }

        public static Key deserialize(byte[] bytes) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            LongBuffer longBuffer = buffer.asLongBuffer();
            UUID id = new UUID(longBuffer.get(), longBuffer.get());
            return new Key(id, longBuffer.get(), longBuffer.get());
        }

        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(32);
            LongBuffer longBuffer = buffer.asLongBuffer();
            longBuffer.put(id.getMostSignificantBits());
            longBuffer.put(id.getLeastSignificantBits());
            longBuffer.put(sequence);
            longBuffer.put(schemaFingerprint);

            return buffer.array();
        }

        @Override
        public String toString() {
            return "Key{" +
                    "id=" + id +
                    ", sequence=" + sequence +
                    ", schemaFingerprint=" + schemaFingerprint +
                    '}';
        }
    }
}
