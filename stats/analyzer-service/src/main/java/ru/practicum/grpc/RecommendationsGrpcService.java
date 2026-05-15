package ru.practicum.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.model.EventSimilarityEntity;
import ru.practicum.service.RecommendationService;

import java.util.List;
import java.util.Map;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsGrpcService extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            long eventId = request.getEventId();
            long userId = request.getUserId();
            int maxResults = request.getMaxResults();

            log.info("gRPC getSimilarEvents: eventId={}, userId={}, maxResults={}",
                    eventId, userId, maxResults);

            List<EventSimilarityEntity> similarEvents =
                    recommendationService.getSimilarEvents(eventId, userId, maxResults);

            for (EventSimilarityEntity entity : similarEvents) {
                long recommendationEvent = entity.getEventA().equals(eventId)
                        ? entity.getEventB() : entity.getEventA();

                RecommendedEventProto response = RecommendedEventProto.newBuilder()
                        .setEventId(recommendationEvent)
                        .setScore(entity.getScore())
                        .build();
                responseObserver.onNext(response);
            }

            responseObserver.onCompleted();
            log.info("Отправлено {} похожих событий для eventId={}", similarEvents.size(), eventId);

        } catch (Exception ex) {
            log.error("Error gRPC getSimilarEvents", ex);
            responseObserver.onError(ex);
        }

    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            long userId = request.getUserId();
            int maxResults = request.getMaxResults();

            log.info("gRPC getRecommendationsForUser: userId={}, maxResult={}",
                    userId, maxResults);

            List<Long> recommendations = recommendationService.getRecommendationsForUser(userId, maxResults);
            Map<Long, Double> scores = recommendationService.getInteractionsCount(recommendations);

            for (Long eventId : recommendations) {
                 RecommendedEventProto response = RecommendedEventProto.newBuilder()
                        .setEventId(eventId)
                        .setScore(scores.getOrDefault(eventId, 0.0))
                        .build();
                responseObserver.onNext(response);
            }

            responseObserver.onCompleted();
            log.info("Отправлено {} рекомендаций для userId={}", recommendations.size(), userId);

        } catch (Exception ex) {
            log.error("Error gRPC getRecommendationsForUser", ex);
            responseObserver.onError(ex);
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<Long> eventIds = request.getEventIdList();
            log.info("gRPC getInteractionsCount: {} events", eventIds.size());

            Map<Long, Double> scores = recommendationService.getInteractionsCount(eventIds);

            for (Long eventId : eventIds) {
                RecommendedEventProto response = RecommendedEventProto.newBuilder()
                        .setEventId(eventId)
                        .setScore(scores.getOrDefault(eventId, 0.0))
                        .build();
                responseObserver.onNext(response);
            }

            responseObserver.onCompleted();
            log.info("Отправлено количество взаимодействий для {} событий", eventIds.size());

        } catch (Exception ex) {
            log.error("Error gRPC getInteractionsCount", ex);
            responseObserver.onError(ex);
        }
    }
}
