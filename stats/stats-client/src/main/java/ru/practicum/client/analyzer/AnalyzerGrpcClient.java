package ru.practicum.client.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerGrpcClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerStub;


    public Stream<RecommendedEventProto> getRecommendationsForUserStream(Long userId, int maxResults) {
        try {
            UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator = analyzerStub.getRecommendationsForUser(request);

            return asStream(iterator);

        } catch (Exception e) {
            log.error("Ошибка при получении рекомендаций для userId={}", userId, e);
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getSimilarEventsStream(Long eventId, Long userId, int maxResults) {
        try {
            SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator = analyzerStub.getSimilarEvents(request);

            return asStream(iterator);

        } catch (Exception e) {
            log.error("Не удалось получить похожие события для eventId={}", eventId, e);
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getInteractionsCountStream(List<Long> eventIds) {
        try {
            InteractionsCountRequestProto.Builder builder = InteractionsCountRequestProto.newBuilder();
            for (Long eventId : eventIds) {
                builder.addEventId(eventId);
            }

            Iterator<RecommendedEventProto> iterator = analyzerStub.getInteractionsCount(builder.build());

            return asStream(iterator);

        } catch (Exception e) {
            log.error("Не удалось получить количество взаимодействий", e);
            return Stream.empty();
        }
    }

    public List<RecommendedEventProto> getRecommendationsForUser(Long userId, int maxResults) {
        return getRecommendationsForUserStream(userId, maxResults)
                .collect(Collectors.toList());
    }

    public List<RecommendedEventProto> getSimilarEvents(Long eventId, Long userId, int maxResults) {
        return getSimilarEventsStream(eventId, userId, maxResults)
                .collect(Collectors.toList());
    }

    public List<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        return getInteractionsCountStream(eventIds)
                .collect(Collectors.toList());
    }

    public Double getEventRating(Long eventId) {
        return getInteractionsCount(List.of(eventId)).stream()
                .findFirst()
                .map(RecommendedEventProto::getScore)
                .orElse(0.0);
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
