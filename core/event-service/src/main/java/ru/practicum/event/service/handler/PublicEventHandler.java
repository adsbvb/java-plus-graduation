package ru.practicum.event.service.handler;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.client.UserClient;
import ru.practicum.client.analyzer.AnalyzerGrpcClient;
import ru.practicum.client.collector.CollectorGrpcClient;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.enums.EventSort;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicEventHandler {
    private final EventRepository eventRepository;
    private final UserClient userClient;

    private final CollectorGrpcClient collectorGrpcClient;
    private final AnalyzerGrpcClient analyzerGrpcClient;

    private static final String URI_EVENT_ENDPOINT = "/events/";
    private static final int MAX_YEARS_RANGE = 1000;

    public List<EventShortDto> getEvents(String text,
                                         List<Long> categories,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Boolean onlyAvailable,
                                         EventSort sort,
                                         Integer from,
                                         Integer size,
                                         HttpServletRequest request) {

        log.info("PublicEventHandler: поиск событий с параметрами: text={}, categories={}, paid={}, sort={}",
                text, categories, paid, sort);

        LocalDateTime start = normalizeStartDate(rangeStart);
        LocalDateTime end = normalizeEndDate(rangeEnd);

        validateDateRange(start, end);

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> eventsList = eventRepository.findPublicEvents(
                text, categories, paid, start, end, onlyAvailable, pageable);

        if (eventsList.isEmpty()) {
            log.info("События не найдены по заданным параметрам");
            return Collections.emptyList();
        }

        Map<Long, UserShortDto> userMap = getUsersMap(eventsList);

        Map<Long, Double> ratingMap = getRatingsForEvents(eventsList);

        List<EventShortDto> result = eventsList.stream()
                .map(event -> {
                    UserShortDto user = userMap.get(event.getInitiatorId());
                    Double rating = ratingMap.getOrDefault(event.getId(), 0.0);
                    return EventMapper.toEventShortDto(event, user, rating);
                })
                .toList();

        if (sort == EventSort.VIEWS) {
            result = result.stream()
                    .sorted(Comparator.comparingDouble(EventShortDto::getRating).reversed())
                    .toList();
        }

        log.info("Найдено {} событий", result.size());
        return result;
    }

    public EventFullDto getById(Long eventId, Long userId) {
        log.info("PublicEventHandler: поиск события с eventId: {}", eventId);

        Event event = eventRepository.findPublishedById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с eventId: %d не найдено", eventId)));
        UserShortDto initiator = getUserById(event.getInitiatorId());
        if (userId != null) {
            collectorGrpcClient.sendView(userId, eventId);
        }
        Double rating = analyzerGrpcClient.getEventRating(eventId);

        return EventMapper.toEventFullDto(event, initiator, rating);
    }

    public List<EventShortDto> getRecommendationsForUser(Long userId, int maxResults) {
        log.info("Получения рекомендаций для пользователя: userId={}, maxResults={}", userId, maxResults);

        List<RecommendedEventProto> recommendations = analyzerGrpcClient
                .getRecommendationsForUser(userId, maxResults);

        if (recommendations.isEmpty()) {
            log.debug("Рекомендации не найдены, userId={}", userId);
            return List.of();
        }

        List<Long> eventIds = recommendations.stream()
                .map(RecommendedEventProto::getEventId)
                .collect(Collectors.toList());

        List<Event> events = eventRepository.findAllById(eventIds);

        Map<Long, UserShortDto> userMap = getUsersMap(events);
        Map<Long, Double> ratingMap = getRatingsForEvents(events);

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    UserShortDto user = userMap.get(event.getInitiatorId());
                    Double rating = ratingMap.getOrDefault(event.getId(), 0.0);
                    return EventMapper.toEventShortDto(event, user, rating);
                })
                .toList();

        log.info("Получено {} рекомендаций для пользователя id={}", result.size(), userId);
        return result;
    }

    public void likeEvent(Long eventId, Long userId) {
        collectorGrpcClient.sendLike(eventId, userId);
        log.info("Пользователь id={} поставил лайк событию id={}", userId, eventId);
    }

    // Private

    private Map<Long, Double> getRatingsForEvents(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        try {
            List<RecommendedEventProto> ratings = analyzerGrpcClient.getInteractionsCount(eventIds);
            return ratings.stream()
                    .collect(Collectors.toMap(
                            RecommendedEventProto::getEventId,
                            RecommendedEventProto::getScore,
                            (v1, v2) -> v1
                    ));

        } catch (Exception e) {
            log.error("Ошибка при получении рейтингов из Analyzer: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private LocalDateTime normalizeStartDate(LocalDateTime rangeStart) {
        return rangeStart != null ? rangeStart : LocalDateTime.now();
    }

    private LocalDateTime normalizeEndDate(LocalDateTime rangeEnd) {
        return rangeEnd != null ? rangeEnd : LocalDateTime.now().plusYears(MAX_YEARS_RANGE);
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }
    }

    private Map<Long, UserShortDto> getUsersMap(List<Event> events) {
        List<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();

        try {
            List<UserShortDto> users = userClient.getUsersInternal(userIds);
            return users.stream()
                    .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));
        } catch (FeignException ex) {
            log.error("Ошибка при вызове сервиса пользователей: {}", ex.getMessage());
            throw new ServiceException("Ошибка при вызове сервиса пользователей", ex);
        }
    }

    private UserShortDto getUserById(Long userId) {
        try {
            return userClient.getUserByIdInternal(userId);
        } catch (FeignException.NotFound ex) {
            log.warn("Пользователь не найден, id: {}", userId);
            throw new NotFoundException("Пользователь не найден, id: " + userId);
        } catch (FeignException ex) {
            log.error("Ошибка при вызове сервиса пользователей: {}", ex.getMessage());
            throw new ServiceException("Ошибка при вызове сервиса пользователей", ex);
        }
    }
}