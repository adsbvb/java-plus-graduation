package ru.practicum.kafka.deserializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Slf4j
public class UserActionAvroDeserializer implements Deserializer<UserActionAvro> {

    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private final Schema schema = UserActionAvro.getClassSchema();

    @Override
    public UserActionAvro deserialize(String topic, byte[] data) {
        if (data == null) {
            log.warn("Получены пустые данные для topic: {}", topic);
            return null;
        }

        try {
            SpecificDatumReader<UserActionAvro> reader = new SpecificDatumReader<>(schema);
            UserActionAvro result = reader.read(null, decoderFactory.binaryDecoder(data, null));

            log.debug("Десериализация UserActionAvro: userId={}, eventId={}, type={}",
                    result.getUserId(), result.getEventId(), result.getActionType());
            return result;

        } catch (Exception ex) {
            log.error("Не удалось десериализовать сообщение для топика: {}", topic, ex);
            throw new RuntimeException("Не удалось десериализовать сообщение для топика: " + topic, ex);
        }
    }
}