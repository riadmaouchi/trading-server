package org.trading.eventstore.store.ldb;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.trading.eventstore.domain.IDRepository;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
import static org.slf4j.LoggerFactory.getLogger;

public class LdbRepository implements IDRepository<String, UUID> {
    private static final Logger logger = getLogger(LdbRepository.class);
    private final DB database;

    public LdbRepository(String path) {
        Options options = new Options();
        options.createIfMissing(true);
        try {
            File file = new File(path, "database.leveldb");
            database = factory.open(file, options);
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
    }

    public void store(String key, UUID value) {
        database.put(bytes(key), bytes(value.toString()));
    }

    public Optional<UUID> load(String key) {
        return ofNullable(asString(database.get(bytes(key)))).map(UUID::fromString);
    }

    public void close() throws IOException {
        try {
            database.close();
        } finally {
            database.close();
        }
    }
}
