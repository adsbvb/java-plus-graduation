package ru.practicum.event.service.handler;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dal.CategoryRepository;
import ru.practicum.category.model.Category;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;
import ru.practicum.client.analyzer.AnalyzerGrpcClient;
import ru.practicum.dto.*;
import ru.practicum.enums.State;
import ru.practicum.enums.Status;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
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
public class PrivateEventHandler {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final AnalyzerGrpcClient analyzerGrpcClient;

    private static final String URI_EVENT_ENDPOINT = "/events/";

    public List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size) {
        log.info("Получение событий, добавленных пользователем id={}", userId);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Event> eventPage = eventRepository.findByInitiatorId(userId, pageable);

        if (eventPage.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventPage.getContent();

        Map<Long, UserShortDto> userMap = getUsersMap(eventPage.getContent());
        Map<Long, Double> ratingMap = getRatingsForEvents(events);

        List<EventShortDto> dtos = events.stream()
                .map(event -> {
                    UserShortDto user = userMap.get(event.getInitiatorId());
                    Double rating = ratingMap.getOrDefault(event.getId(), 0.0);
                    return EventMapper.toEventShortDto(event, user, rating);
                })
                .collect(Collectors.toList());

        log.info("Для пользователя id={} найдено {} событий", userId, dtos.size());
        return dtos;
    }

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Добавление события {} пользователем id={}", newEventDto.getTitle(), userId);

        validateEventDate(newEventDto.getEventDate());

        Category category = getCategoryById(newEventDto.getCategoryId());
        UserShortDto user = getUserById(userId);

        Event event = eventRepository.save(EventMapper.newEventDtoToEvent(newEventDto, userId, category));

        log.info("Событие успешно добавлено, id: {}", event.getId());
        return EventMapper.toEventFullDto(event, user, 0.0);
    }

    public EventFullDto getInfoEvent(Long userId, Long eventId) {
        log.info("Получение полной информации о событии id={} пользователя id={}", eventId, userId);

        Event event = getEventById(eventId);
        UserShortDto user = getUserById(userId);

        Double rating = analyzerGrpcClient.getEventRating(eventId);

        return EventMapper.toEventFullDto(event, user, rating);
    }

    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        log.info("Изменение события id={} пользователем id={}", eventId, userId);

        Event event = getEventById(eventId);

        if (event.getState() == State.PUBLISHED) {
            log.warn("Нельзя изменить опубликованное событие. Статус: {}", event.getState());
            throw new ConflictException("Нельзя изменить опубликованное событие");
        }

        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Только владелец может редактировать событие");
        }

        Optional<Category> category = Optional.empty();
        if (updateRequest.getCategoryId() != null) {
            category = Optional.of(getCategoryById(updateRequest.getCategoryId()));
        }

        Event updatedEvent = EventMapper.updateEventDtoToEvent(event, updateRequest, category);
        updatedEvent = eventRepository.save(updatedEvent);

        UserShortDto user = getUserById(userId);
        Double rating = analyzerGrpcClient.getEventRating(eventId);

        log.info("Событие {} успешно изменено", updatedEvent.getId());

        return EventMapper.toEventFullDto(updatedEvent, user, rating);
    }

    public List<ParticipationRequestDto> getInfoRequest(Long userId, Long eventId) {
        log.info("Получение запросов на участие в событии id={} пользователя id={}", eventId, userId);

        Event event = getEventById(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            log.warn("Пользователь id={} не является владельцем события id={}", userId, eventId);
            throw new ConflictException("Только владелец события может просматривать заявки");
        }

        List<ParticipationRequestDto> requests;

        try {
            requests = requestClient.getRequestsByEventIdInternal(eventId);
        } catch (FeignException ex) {
            log.error("Ошибка при вызове сервиса запросов: {}", ex.getMessage());
            throw new ServiceException("Ошибка при вызове сервиса запросов", ex);
        }

        log.info("Для события id={} найдено {} запросов", eventId, requests.size());
        return requests;
    }

    @Transactional
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        log.info("Обновление статуса заявок на событие id={} пользователя id={}", eventId, userId);

        Event event = getEventById(eventId);

        validateEventOwnership(event, userId);
        validateModerationRequired(event);

        List<Long> requestIds = updateRequest.getRequestIds();
        Status newStatus = updateRequest.getStatus();

        validateNewStatus(newStatus);

        List<ParticipationRequestDto> requests = getRequestsByIds(requestIds);
        validateRequests(requests, eventId);

        return processRequestUpdates(event, requests, newStatus);
    }

    // Private

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            log.warn("Время события должно быть не менее чем за 2 часа");
            throw new ValidationException("Время события должно быть не менее чем за 2 часа");
        }
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Категория не найдена, id: {}", categoryId);
                    return new NotFoundException("Категория не найдена");
                });
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Событие не найдено, id: {}", eventId);
                    return new NotFoundException("Событие не найдено");
                });
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

    private void validateEventOwnership(Event event, Long userId) {
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Только владелец может изменять статус запросов");
        }
    }

    private void validateModerationRequired(Event event) {
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Подтверждение заявок не требуется для этого события");
        }
    }

    private void validateNewStatus(Status status) {
        if (status != Status.CONFIRMED && status != Status.REJECTED) {
            throw new ConflictException("Недопустимый статус: " + status);
        }
    }

    private List<ParticipationRequestDto> getRequestsByIds(List<Long> requestIds) {
        try {
            return requestClient.getRequestsByIdsInternal(requestIds);
        } catch (FeignException ex) {
            log.error("Ошибка при вызове сервиса запросов: {}", ex.getMessage());
            throw new ServiceException("Ошибка при вызове сервиса запросов", ex);
        }
    }

    private void validateRequests(List<ParticipationRequestDto> requests, Long eventId) {
        for (ParticipationRequestDto request : requests) {
            if (request.getStatus() != Status.PENDING) {
                throw new ConflictException("Статус заявки " + request.getId() +
                        " нельзя изменить (текущий: " + request.getStatus() + ")");
            }
            if (!request.getEventId().equals(eventId)) {
                throw new ConflictException("Заявка " + request.getId() + " не относится к событию");
            }
        }
    }

    private EventRequestStatusUpdateResult processRequestUpdates(Event event,
                                                                 List<ParticipationRequestDto> requests,
                                                                 Status newStatus) {
        long confirmedRequests = event.getConfirmedRequests();
        long participantLimit = event.getParticipantLimit();
        long availableSlots = participantLimit - confirmedRequests;

        if (newStatus == Status.CONFIRMED) {
            if (availableSlots <= 0 && !requests.isEmpty()) {
                throw new ConflictException("Достигнут лимит участников события. Лимит: " +
                        participantLimit + ", уже подтверждено: " + confirmedRequests);
            }
            if (requests.size() > availableSlots) {
                throw new ConflictException("Невозможно подтвердить " + requests.size() +
                        " заявок. Доступно свободных слотов: " + availableSlots);
            }
        }

        List<ParticipationRequestDto> confirmedDtos = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDtos = new ArrayList<>();

        if (newStatus == Status.CONFIRMED) {
            if (availableSlots <= 0) {
                requests.forEach(r -> {
                    r.setStatus(Status.REJECTED);
                    rejectedDtos.add(r);
                });
            } else {
                long toConfirm = Math.min(requests.size(), availableSlots);
                for (int i = 0; i < requests.size(); i++) {
                    ParticipationRequestDto request = requests.get(i);
                    if (i < toConfirm) {
                        request.setStatus(Status.CONFIRMED);
                        confirmedDtos.add(request);
                    } else {
                        request.setStatus(Status.REJECTED);
                        rejectedDtos.add(request);
                    }
                }
            }
        } else if (newStatus == Status.REJECTED) {
            requests.forEach(r -> {
                r.setStatus(Status.REJECTED);
                rejectedDtos.add(r);
            });
        }

        updateRequestsAndEvent(event, requests, confirmedDtos.size());

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedDtos)
                .rejectedRequests(rejectedDtos)
                .build();
    }

    private void updateRequestsAndEvent(Event event, List<ParticipationRequestDto> requests, int confirmedCount) {
        try {
            requestClient.batchUpdateRequestsIternal(requests);

            if (confirmedCount > 0) {
                event.setConfirmedRequests(event.getConfirmedRequests() + confirmedCount);
                eventRepository.save(event);
            }
        } catch (FeignException ex) {
            log.error("Ошибка при обновлении запросов: {}", ex.getMessage());
            throw new ServiceException("Ошибка при обновлении запросов", ex);
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
}