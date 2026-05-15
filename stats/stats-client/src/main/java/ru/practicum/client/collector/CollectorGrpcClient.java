package ru.practicum.client.collector;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectorGrpcClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub stub;

    public void sendUserAction(Long userId, Long eventId, ActionTypeProto action) {
        try {
            Timestamp timestamp = Timestamp.newBuilder()
                    .setSeconds(Instant.now().getEpochSecond())
                    .setNanos(Instant.now().getNano())
                    .build();

            UserActionProto request = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(action)
                    .setTimestamp(timestamp)
                    .build();

            Empty response = stub.collectUserAction(request);
            log.debug("Отправлено действие пользователя: userId={}, eventId={}, type={}", userId, eventId, action);

        } catch (Exception ex) {
            log.error("Не удалось отправить действие пользователя", ex);
        }
    }

    public void sendView(Long userId, Long eventId) {
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    public void sendRegister(Long userId, Long eventId) {
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }

    public void sendLike(Long userId, Long eventId) {
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }
}
