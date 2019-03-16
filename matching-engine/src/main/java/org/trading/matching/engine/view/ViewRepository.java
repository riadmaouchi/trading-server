package org.trading.matching.engine.view;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.trading.MessageProvider;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
import static org.slf4j.LoggerFactory.getLogger;

public class ViewRepository {
    private static final Logger logger = getLogger(ViewRepository.class);
    private final DB database;
    private final MessageSerializer messageSerializer = new MessageSerializer();

    public ViewRepository(String path) {
        Options options = new Options();
        options.createIfMissing(true);
        try {
            File file = new File(path, "views.database.leveldb");
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

    public void store(MessageProvider.Message message) {
        byte[] timestamp = ByteBuffer.allocate(8).putLong(System.nanoTime()).array();
        database.put(timestamp, messageSerializer.serialize(message));
    }

    public List<MessageProvider.Message> loadAll() {
        DBIterator iterator = null;

        try {
            iterator = database.iterator();

            List<MessageProvider.Message> events = new ArrayList<>();

            while (iterator.hasNext()) {
                Map.Entry<byte[], byte[]> entry = iterator.next();
                events.add(messageSerializer.deserialize(entry.getValue()));
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

    public void close() throws IOException {
        try {
            database.close();
        } finally {
            database.close();
        }
    }
}
