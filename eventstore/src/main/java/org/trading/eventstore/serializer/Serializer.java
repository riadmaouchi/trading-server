package org.trading.eventstore.serializer;

import org.apache.avro.Schema;
import org.trading.eventstore.domain.Event;

public interface Serializer<T extends Event> {

    Class getEventClass();

    Schema getSchema();

    T deserialize(byte[] bytes, Schema writerSchema);

    byte[] serialize(T event);


}
