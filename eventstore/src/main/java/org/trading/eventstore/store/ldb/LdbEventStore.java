package org.trading.eventstore.store.ldb;

import org.apache.avro.Schema;
import org.apache.avro.SchemaNormalization;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.trading.eventstore.domain.Event;
import org.trading.eventstore.serializer.Serializer;
import org.trading.eventstore.store.EventStore;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
import static org.slf4j.LoggerFactory.getLogger;

public class LdbEventStore<T extends Event> extends EventStore<T> {

    private static final Logger logger = getLogger(LdbEventStore.class);

    private final DB eventDatabase;
    private final DB schemaDatabase;

    public LdbEventStore(String path, Collection<Serializer> serializers) {
        super(serializers);
        Options options = new Options();
        options.createIfMissing(true);
        try {
            eventDatabase = factory.open(new File(path, "events.leveldb"), options);
            schemaDatabase = factory.open(new File(path, "schemas.leveldb"), options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                close();
            } catch (IOException e) {
                logger.error("Error while closing LevelDB file", e);
            }
        }));

        for (Serializer serializer : serializers) {
            Schema schema = serializer.getSchema();
            long fingerprint = SchemaNormalization.parsingFingerprint64(schema);
            byte[] fingerprintData = ByteBuffer.allocate(8).putLong(fingerprint).array();
            byte[] data = schemaDatabase.get(fingerprintData);
            if (data == null) {
                schemaDatabase.put(fingerprintData, schema.toString().getBytes());
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            schemaDatabase.close();
        } finally {
            eventDatabase.close();
        }
    }

    @Override
    protected void store(Key key, byte[] data) {
        eventDatabase.put(key.toBytes(), data);
    }

    @Override
    protected Schema loadSchema(long fingerprint) {
        byte[] fingerprintData = ByteBuffer.allocate(8).putLong(fingerprint).array();
        byte[] data = schemaDatabase.get(fingerprintData);
        if (data == null) {
            throw new RuntimeException("Schema not found for fingerprint: " + fingerprint);
        }
        return new Schema.Parser().parse(new String(data));
    }

    @Override
    public List<T> loadEventStream(UUID aggregateId) {
        Objects.requireNonNull(aggregateId);
        DBIterator iterator = null;

        try {
            iterator = eventDatabase.iterator();
            iterator.seek(new Key(aggregateId).toBytes());

            List<T> events = new ArrayList<>();

            boolean keepMoving = true;
            while (keepMoving && iterator.hasNext()) {
                Map.Entry<byte[], byte[]> entry = iterator.next();
                Key key = Key.deserialize(entry.getKey());
                if (!aggregateId.equals(key.id)) {
                    keepMoving = false;
                } else {
                    events.add(deserializeEvent(entry.getValue(), key.schemaFingerprint));
                }
            }
            return events;
        } finally {
            if (iterator != null) {
                try {
                    iterator.close();
                } catch (IOException e) {
                    logger.error("Error while closing LevelDB iterator", e);
                }
            }
        }
    }
}
