package ru.practicum.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorGrpcService extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final KafkaTemplate<Long, UserActionAvro> kafkaTemplate;

    @Value("${kafka.topic.user-actions:stats.user-actions.v1}")
    private String userActionsTopic;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> response) {
        try {
            log.info("Получены действия пользователя: userId={}, eventId={}, type={}",
                    request.getUserId(), request.getEventId(), request.getActionType());

            UserActionAvro avro = convertToAvro(request);

            kafkaTemplate.send(userActionsTopic, avro.getUserId(), avro)
                    .whenComplete((result, error) -> {
                        if (error == null) {
                            log.info("Сообщение отправлено в Kafka: topic={}, partition={}, offset={}",
                                    userActionsTopic,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());

                            response.onNext(Empty.getDefaultInstance());
                            response.onCompleted();
                        }
                    });
        } catch (Exception ex) {
            log.error("Ошибка обработки действий пользователя", ex);
            response.onError(ex);
        }
    }

    private UserActionAvro convertToAvro(UserActionProto proto) {
        ActionTypeAvro actionTypeAvro = convertActionType(proto.getActionType());
        return UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(actionTypeAvro)
                .setTimestamp(Instant.ofEpochSecond(proto.getTimestamp().getSeconds(),
                        proto.getTimestamp().getNanos()))
                .build();
    }

    private ActionTypeAvro convertActionType(ActionTypeProto protoType) {
        switch (protoType) {
            case ACTION_VIEW:
                return ActionTypeAvro.VIEW;
            case ACTION_REGISTER:
                return ActionTypeAvro.REGISTER;
            case ACTION_LIKE:
                return ActionTypeAvro.LIKE;
            default:
                throw new IllegalArgumentException("Некоректный тип действия: " + protoType);
        }
    }
}
