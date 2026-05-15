package ru.practicum.kafka.deserializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Slf4j
public class EventSimilarityAvroDeserializer implements Deserializer<EventSimilarityAvro> {

    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private final Schema schema = EventSimilarityAvro.getClassSchema();

    @Override
    public EventSimilarityAvro deserialize(String topic, byte[] data) {
        if (data == null) {
            log.warn("Получены пустые данные для topic: {}", topic);
            return null;
        }

        try {
            SpecificDatumReader<EventSimilarityAvro> reader = new SpecificDatumReader<>(schema);
            EventSimilarityAvro result = reader.read(null, decoderFactory.binaryDecoder(data, null));

            log.debug("Десериализация EventSimilarityAvro: eventA={}, eventB={}, score={}",
                    result.getEventA(), result.getEventB(), result.getScore());

            return result;

        } catch (Exception ex) {
            log.error("Не удалось десериализовать сообщение для топика: {}", topic, ex);
            throw new RuntimeException("Не удалось десериализовать сообщение для топика: " + topic, ex);
        }
    }
}