package org.trading.eventstore.serializer;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.util.Utf8;
import org.trading.eventstore.domain.Event;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public abstract class EventSerializer<T extends Event> implements Serializer<T> {

    protected SchemaBuilder.FieldAssembler<Schema> getEventSchema(String eventName) {
        return SchemaBuilder
                .record(eventName).namespace(EventSerializer.class.getPackageName())
                .fields()
                .name("id").type().stringType().noDefault()
                .name("sequence").type().longType().noDefault();
    }

    @Override
    public T deserialize(byte[] bytes, Schema writerSchema) {
        Schema SCHEMA = getSchema();

        DatumReader<GenericRecord> datumReader;
        if (writerSchema == null) {
            datumReader = new GenericDatumReader<>(SCHEMA);
        } else {
            datumReader = new GenericDatumReader<>(writerSchema, SCHEMA);
        }

        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        GenericRecord record;
        try {
            record = datumReader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UUID id = UUID.fromString(((Utf8) record.get("id")).toString());
        long sequence = (Long) record.get("sequence");

        return deserialize(id, sequence, record);
    }

    public abstract T deserialize(UUID id, long sequence, GenericRecord record);

    @Override
    public byte[] serialize(T event) {
        Schema SCHEMA = getSchema();
        BinaryEncoder encoder;
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(SCHEMA);
        GenericData.Record record = new GenericData.Record(SCHEMA);
        record.put("id", event.getAggregateId().toString());
        record.put("sequence", event.getSequence());

        serialize(record, event);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        encoder = EncoderFactory.get().binaryEncoder(stream, null);
        try {
            datumWriter.write(record, encoder);
            encoder.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stream.toByteArray();
    }

    public abstract void serialize(GenericData.Record record, T event);
}