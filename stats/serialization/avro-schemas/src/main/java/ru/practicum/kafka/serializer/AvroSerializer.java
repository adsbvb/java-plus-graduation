package ru.practicum.kafka.serializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Serializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class AvroSerializer<T> implements Serializer<T> {

    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            log.warn("Получены пустые данные для topic: {}", topic);
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            SpecificRecordBase record;
            Schema schema;

            if (data instanceof UserActionAvro avroData) {
                record = avroData;
                schema = avroData.getSchema();
                log.debug("Сериализация UserActionAvro: userId={}, eventId={}",
                        avroData.getUserId(), avroData.getEventId());
            } else if (data instanceof EventSimilarityAvro avroData) {
                record = avroData;
                schema = avroData.getSchema();
                log.debug("Сериализация EventSimilarityAvro: eventA={}, eventB={}, score={}",
                        avroData.getEventA(), avroData.getEventB(), avroData.getScore());
            } else {
                throw new IllegalArgumentException("Неподдерживаемый тип Avro: " + data.getClass().getName());
            }

            BinaryEncoder encoder = encoderFactory.binaryEncoder(outputStream, null);
            SpecificDatumWriter<SpecificRecordBase> datumWriter = new SpecificDatumWriter<>(schema);

            datumWriter.write(record, encoder);
            encoder.flush();

            byte[] result = outputStream.toByteArray();
            log.debug("Сериализация {}: {} байтов", data.getClass().getSimpleName(), result.length);
            return result;

        } catch (IOException ex) {
            log.error("Не удалось сериализовать сообщение для топика: {}", topic, ex);
            throw new RuntimeException("Не удалось сериализовать сообщение для топика " + topic, ex);
        }
    }
}