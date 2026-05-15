package ru.practicum.event.service.handler;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dal.CategoryRepository;
import ru.practicum.category.model.Category;
import ru.practicum.client.UserClient;
import ru.practicum.client.analyzer.AnalyzerGrpcClient;
import ru.practicum.dto.AdminEventParam;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.dto.UserShortDto;
import ru.practicum.enums.AdminStateAction;
import ru.practicum.enums.State;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminEventHandler {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserClient userClient;
    private final AnalyzerGrpcClient analyzerGrpcClient;

    private static final int MIN_HOURS_BEFORE_EVENT = 1;

    public List<EventFullDto> getFullEvents(AdminEventParam params) {
        log.info("AdminEventHandler: поиск событий с параметрами: users={}, states={}, categories={}",
                params.getUsers(), params.getStates(), params.getCategories());

        List<State> states = convertStatesEnum(params.getStates());

        int pageNumber = params.getFrom() / params.getSize();
        Pageable pageable = PageRequest.of(pageNumber, params.getSize());

        List<Event> events = eventRepository.findEventByAdmin(
                params.getUsers(),
                states,
                params.getCategories(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageable
        );

        if (events.isEmpty()) {
            log.info("События не найдены");
            return Collections.emptyList();
        }

        Map<Long, UserShortDto> userMap = getUsersMap(events);
        Map<Long, Double> ratingMap = getRatingsForEvents(events);

        List<EventFullDto> result = events.stream()
                .map(event -> {
                    Double rating = ratingMap.getOrDefault(event.getId(), 0.0);
                    UserShortDto initiator = userMap.get(event.getInitiatorId());
                    return EventMapper.toEventFullDto(event, initiator, rating);
                })
                .toList();

        log.info("Найдено {} событий", result.size());
        return result;
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        log.info("AdminEventHandler: обновление события id={} администратором", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено."));

        if (request.getStateAction() != null) {
            processStateAction(event, request.getStateAction());
        }

        updateEventFields(event, request);

        validateEventDate(event.getEventDate());

        Event updatedEvent = eventRepository.save(event);

        UserShortDto user = getUserById(updatedEvent.getInitiatorId());
        Double rating = analyzerGrpcClient.getEventRating(eventId);

        log.info("Событие id={} успешно обновлено, новый статус: {}", eventId, updatedEvent.getState());
        return EventMapper.toEventFullDto(updatedEvent, user, rating);
    }

    // Private

    private List<State> convertStatesEnum(List<String> states) {
        if (states == null || states.isEmpty()) {
            return null;
        }

        return states.stream()
                .map(state -> {
                    try {
                        return State.valueOf(state.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Некорректное состояние события: {}", state);
                        throw new ValidationException("Некорректное состояние события: " + state);
                    }
                })
                .toList();
    }

    private void processStateAction(Event event, AdminStateAction stateAction) {
        if (stateAction == AdminStateAction.PUBLISH_EVENT) {
            publishEvent(event);
        } else if (stateAction == AdminStateAction.REJECT_EVENT) {
            rejectEvent(event);
        }
    }

    private void publishEvent(Event event) {
        if (event.getState() != State.PENDING) {
            throw new ConflictException("Событие можно опубликовать, только если оно в состоянии PENDING. Текущий статус: " + event.getState());
        }

        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
            throw new ValidationException("Дата начала события должна быть не ранее чем через " + MIN_HOURS_BEFORE_EVENT + " час(а)");
        }

        event.setState(State.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
        log.info("Событие id={} опубликовано", event.getId());
    }

    private void rejectEvent(Event event) {
        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("Нельзя отклонить уже опубликованное событие");
        }
        event.setState(State.CANCELED);
        log.info("Событие id={} отклонено", event.getId());
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest request) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + request.getCategoryId() + " не найдена."));
            event.setCategory(category);
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }

        if (request.getLocationDto() != null) {
            event.setLocation(Location.builder()
                    .lat(request.getLocationDto().getLat())
                    .lon(request.getLocationDto().getLon())
                    .build());
        }

        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }

        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата события не может быть в прошлом");
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