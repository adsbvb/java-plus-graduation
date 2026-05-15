package ru.practicum.service;

import ru.practicum.model.EventSimilarityEntity;

import java.util.List;
import java.util.Map;

public interface RecommendationService {
    List<EventSimilarityEntity> getSimilarEvents(Long eventId, Long userId, int maxResults);

    List<Long> getRecommendationsForUser(Long userId, int maxResults);

    Map<Long, Double> getInteractionsCount(List<Long> eventIds);
}
