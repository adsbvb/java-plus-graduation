package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarityAggregationService {

    private final KafkaTemplate<Long, EventSimilarityAvro> kafkaTemplate;

    @Value("${kafka.topic.similarity:stats.events-similarity.v1}")
    private String similarityTopic;

    private final Map<Long, Map<Long, Double>> userMaxWeights = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, Double>> minSums = new ConcurrentHashMap<>();
    private final Map<Long, Double> eventSums = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${kafka.topic.user-actions:stats.user-actions.v1}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeUserActions(List<UserActionAvro> userActions) {
        log.info("Получен batch пакет UserActionAvro: {}", userActions.size());

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        for (UserActionAvro userAction : userActions) {
            try {
                processAction(userAction);
                success.incrementAndGet();
            } catch (Exception ex) {
                log.error("Ошибка при обработке действия", ex);
                errors.incrementAndGet();
            }
        }
        log.info("Обработано: success={}, errors={}", success.get(), errors.get());
    }

    private void processAction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double newWeight = getWeight(action.getActionType());

        log.info("Обработка: userId={}, eventId={}, type={}, weight={}",
                userId, eventId, action.getActionType(), newWeight);

        Map<Long, Double> userEvents = userMaxWeights.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());

        double oldWeight = userEvents.getOrDefault(eventId, 0.0);

        if (newWeight <= oldWeight) {
            log.info("Пропускаем: newWeight={} <= oldWeight={}", newWeight, oldWeight);
            return;
        }

        double deltaSum = newWeight - oldWeight;
        eventSums.merge(eventId, deltaSum, Double::sum);
        log.debug("eventSums[{}] += {} -> {}", eventId, deltaSum, eventSums.get(eventId));

        for (Map.Entry<Long, Double> entry : userEvents.entrySet()) {
            long otherId = entry.getKey();
            if (otherId == eventId) continue;

            double otherWeight = entry.getValue();

            double oldMin = Math.min(oldWeight, otherWeight);
            double newMin = Math.min(newWeight, otherWeight);
            double deltaMin = newMin - oldMin;

            long first = Math.min(eventId, otherId);
            long second = Math.max(eventId, otherId);

            Map<Long, Double> inner = minSums.computeIfAbsent(first, k -> new ConcurrentHashMap<>());
            inner.merge(second, deltaMin, Double::sum);

            double minSum = inner.get(second);
            double sumA = eventSums.getOrDefault(first, 0.0);
            double sumB = eventSums.getOrDefault(second, 0.0);
            double similarity = minSum / (Math.sqrt(sumA) * Math.sqrt(sumB));

            double roundedSimilarity = Math.round(similarity * 1000000.0) / 1000000.0;

            log.info("Обновляем пару ({},{}): deltaMin={}, similarity={}", first, second, deltaMin, roundedSimilarity);

            EventSimilarityAvro message = EventSimilarityAvro.newBuilder()
                    .setEventA(first)
                    .setEventB(second)
                    .setScore(roundedSimilarity)
                    .setTimestamp(action.getTimestamp())
                    .build();

            kafkaTemplate.send(similarityTopic, first, message);
        }

        userEvents.put(eventId, newWeight);
    }

    private double getWeight(ActionTypeAvro actionType) {
        switch (actionType) {
            case VIEW: return 0.4;
            case REGISTER: return 0.8;
            case LIKE: return 1.0;
            default: return 0.0;
        }
    }
}