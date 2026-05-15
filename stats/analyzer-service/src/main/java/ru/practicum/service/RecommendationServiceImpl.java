package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dal.EventSimilarityRepository;
import ru.practicum.dal.UserActionRepository;
import ru.practicum.model.EventSimilarityEntity;
import ru.practicum.model.UserActionEntity;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final UserActionRepository actionRepository;
    private final EventSimilarityRepository similarityRepository;

    @Override
    public List<EventSimilarityEntity> getSimilarEvents(Long eventId, Long userId, int maxResults) {
        log.debug("Поиск похожих событий по: eventId={}, userId={}", eventId, userId);

        List<EventSimilarityEntity> similarEvents = similarityRepository.findSimilarEvents(eventId);

        Set<Long> interactedEvents = actionRepository.findUserInteractedEvents(userId);

        return similarEvents.stream()
                .filter(s -> !interactedEvents.contains(getOtherEventId(s, eventId)))
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(maxResults)
                .toList();
    }

    @Override
    public List<Long> getRecommendationsForUser(Long userId, int maxResults) {
        log.debug("Получение персональных рекомендаций для userId={}", userId);

        List<UserActionEntity> recentActions = actionRepository.findRecentActionsByUserId(userId);

        if (recentActions.isEmpty()) {
            log.debug("У пользователя нет последних действий");
            return Collections.emptyList();
        }

        List<Long> eventIds = recentActions.stream()
                .map(UserActionEntity::getEventId)
                .toList();

        List<EventSimilarityEntity> allSimilar = similarityRepository.findSimilarEventsByEventIds(eventIds);

        Map<Long, List<EventSimilarityEntity>> similarByEventId = allSimilar.stream()
                .collect(Collectors.groupingBy(s ->
                        eventIds.contains(s.getEventA()) ? s.getEventA() : s.getEventB()
                    ));

        Set<Long> interactedEvents = actionRepository.findUserInteractedEvents(userId);

        Map<Long, Double> candidateScores = new HashMap<>();

        for (UserActionEntity action : recentActions) {
            Long eventId = action.getEventId();
            List<EventSimilarityEntity> similar = similarByEventId.getOrDefault(eventId, Collections.emptyList());
            double weight = action.getWeight();

            for (EventSimilarityEntity s : similar) {
                Long candidateId = s.getEventA().equals(eventId)
                        ? s.getEventB() : s.getEventA();
                if (!interactedEvents.contains(candidateId)) {
                    candidateScores.merge(candidateId, s.getScore() * weight, Double::sum);
                }
            }
        }

        return candidateScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, Double> getInteractionsCount(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> results = actionRepository.getTotalWeightForEvents(eventIds);

        return results.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Double) r[1]
                ));
    }

    private Long getOtherEventId(EventSimilarityEntity similarEvent, Long eventId) {
        return similarEvent.getEventA().equals(eventId) ? similarEvent.getEventB() : eventId;
    }
}
