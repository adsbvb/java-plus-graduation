package ru.practicum.event.service.handler;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.client.StatClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.Constant;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.dto.response.HitsCounterResponseDto;
import ru.practicum.enums.EventSort;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final StatClient statClient;
    private final UserClient userClient;

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

        Map<Long, Long> viewsMap = getViewsForEvents(eventsList, start, end);

        List<EventShortDto> result = eventsList.stream()
                .map(event -> {
                    UserShortDto user = userMap.get(event.getInitiatorId());
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    return EventMapper.toEventShortDtoWithViews(event, user, views);
                })
                .toList();

        if (sort == EventSort.VIEWS) {
            result = result.stream()
                    .sorted(Comparator.comparingLong(EventShortDto::getViews).reversed())
                    .toList();
        }

        sendHitToStats(request);

        log.info("Найдено {} событий", result.size());
        return result;
    }

    public EventFullDto getById(Long id, HttpServletRequest request) {
        log.info("PublicEventHandler: поиск события с id: {}", id);

        Event event = eventRepository.findPublishedById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id: %d не найдено", id)));

        UserShortDto initiator = getUserById(event.getInitiatorId());

        sendHitToStats(request);

        Long views = getViewsForEvent(event);

        return EventMapper.toEventFullDto(event, initiator, views);
    }

    // Private

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

    private Map<Long, Long> getViewsForEvents(List<Event> events, LocalDateTime start, LocalDateTime end) {
        List<String> eventUris = events.stream()
                .map(event -> URI_EVENT_ENDPOINT + event.getId())
                .toList();

        log.debug("Запрос статистики для {} событий за период {} - {}", events.size(), start, end);

        List<HitsCounterResponseDto> hitsCounterList;
        try {
            hitsCounterList = statClient.getHits(start, end, eventUris, false);
        } catch (FeignException ex) {
            log.error("Ошибка при вызове сервиса статистики: {}", ex.getMessage());
            return Collections.emptyMap();
        }

        log.debug("Получено {} записей статистики", hitsCounterList.size());

        return hitsCounterList.stream()
                .collect(Collectors.toMap(
                        hitsCounter -> EventMapper.extractIdFromUri(hitsCounter.getUri()),
                        HitsCounterResponseDto::getHits,
                        (v1, v2) -> v1
                ));
    }

    private Long getViewsForEvent(Event event) {
        LocalDateTime start = event.getPublishedOn();
        LocalDateTime end = LocalDateTime.now();

        if (start == null) {
            log.warn("Событие {} еще не опубликовано, просмотров 0", event.getId());
            return 0L;
        }

        try {
            List<HitsCounterResponseDto> hitsCounter = statClient.getHits(
                    start,
                    end,
                    List.of(URI_EVENT_ENDPOINT + event.getId()),
                    true);

            return hitsCounter.isEmpty() ? 0L : hitsCounter.getFirst().getHits();
        } catch (FeignException ex) {
            log.error("Ошибка при получении статистики для события {}: {}", event.getId(), ex.getMessage());
            return 0L;
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

    private void sendHitToStats(HttpServletRequest request) {
        try {
            statClient.hit(new StatHitRequestDto(
                    Constant.SERVICE_POSTFIX,
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT))
            ));
        } catch (FeignException ex) {
            log.error("Ошибка при отправке статистики запроса: {}", ex.getMessage());
        }
    }
}