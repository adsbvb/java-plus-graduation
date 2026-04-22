package ru.practicum.service.private_ewm.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatClient;
import ru.practicum.dto.response.HitsCounterResponseDto;
import ru.practicum.service.dal.CategoryRepository;
import ru.practicum.service.dal.EventRepository;
import ru.practicum.service.dal.RequestRepository;
import ru.practicum.service.dal.UserRepository;
import ru.practicum.service.dto.*;
import ru.practicum.service.error.ConflictException;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.error.ValidationException;
import ru.practicum.service.mapper.EventMapper;
import ru.practicum.service.mapper.RequestMapper;
import ru.practicum.service.model.Category;
import ru.practicum.service.model.Event;
import ru.practicum.service.model.Request;
import ru.practicum.service.model.User;
import ru.practicum.service.model.enums.State;
import ru.practicum.service.model.enums.Status;
import ru.practicum.service.statistics.StatisticsService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrivateServiceImpl implements PrivateService {
    private static final Logger log = LoggerFactory.getLogger(PrivateServiceImpl.class);
    private static final String URI_EVENT_ENDPOINT = "/events/";

    private final StatClient client;
    private final StatisticsService statsService;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    private Map<String, Long> getViewsForEvents(List<EventShortDto> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> uris = events.stream()
                .map(dto -> URI_EVENT_ENDPOINT + dto.getId())
                .collect(Collectors.toList());
        return statsService.getViewsByUris(uris, false);
    }

    @Override
    public List<EventShortDto> getEventsByOwner(Long userId, Long from, Long size) {
        int page = from.intValue() / size.intValue();
        Pageable pageable = PageRequest.of(page, size.intValue());

        Page<Event> eventPage = eventRepository.findByInitiator_Id(userId, pageable);
        List<EventShortDto> dtos = eventPage.getContent().stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        Map<String, Long> viewsMap = getViewsForEvents(dtos);

        dtos.forEach(dto -> {
            String uriKey = URI_EVENT_ENDPOINT + dto.getId();
            Long views = viewsMap.getOrDefault(uriKey, 0L);
            dto.setViews(views);
        });

        return dtos;
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Время события должно быть за два часа до события.");
        }

        Category category = categoryRepository.findById(newEventDto.getCategoryId())
                .orElseThrow(() -> new ValidationException("Категория не указана"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует."));

        Event event = eventRepository.save(EventMapper.newEventDtoToEvent(newEventDto, user, category));

        return EventMapper.eventToEventFullDto(event);
    }

    @Override
    public EventFullDto getInfoEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует."));
        EventFullDto eventFullDto = EventMapper.eventToEventFullDto(event);

        List<HitsCounterResponseDto> hitsCounter = client.getHits(
                List.of(URI_EVENT_ENDPOINT + eventFullDto.getId()),
                true);
        Long views = hitsCounter.isEmpty() ? 0L : hitsCounter.getFirst().getHits();

        eventFullDto.setViews(views);
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует."));
        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Данное событие нельзя изменить.");
        }
        Optional<Category> category = Optional.empty();
        if (updateEventUserRequest.getCategoryId() != null) {
            category = categoryRepository.findById(updateEventUserRequest.getCategoryId());
        }
        Event updateEvent = eventRepository
                .save(EventMapper.updateEventDtoToEvent(event, updateEventUserRequest, category));

        return EventMapper.eventToEventFullDto(updateEvent);
    }

    @Override
    public List<ParticipationRequestDto> getInfoRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ValidationException("Событие не найдено"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));
        return requestRepository.findAllByEvent(event).stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Только владелец может обновить статус запроса");
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Подтверждение заявок не требуется");
        }

        List<Long> userIds = updateRequest.getRequestIds();
        Status status = updateRequest.getStatus();

        List<Request> requests = requestRepository.findAllById(userIds);

        for (Request request : requests) {
            if (request.getStatus() != Status.PENDING) {
                throw new ConflictException("Статус заявки ID=" + request.getId() +
                        " нельзя изменить: текущий статус — " + request.getStatus());
            }
        }

        Map<Long, Request> requestMap = requests.stream()
                .collect(Collectors.toMap(Request::getId, request -> request));

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        if (status.equals(Status.CONFIRMED)) {
            long confirmed = event.getConfirmedRequests();
            long limit = event.getParticipantLimit();

            if (limit > 0 && confirmed >= limit) {
                throw new ConflictException("Достигнут лимит участников события");
            }

            long availableSlots = (limit == 0) ? requests.size() : limit - confirmed;

            if (availableSlots <= 0) {
                for (Long id : userIds) {
                    Request request = requestMap.get(id);
                    if (request != null) {
                        request.setStatus(Status.REJECTED);
                        rejectedRequests.add(RequestMapper.toRequestDto(request));
                    }
                }
            } else {
                int confirmedCount = 0;

                for (Long id : userIds) {
                    Request request = requestMap.get(id);
                    if (request == null) continue;

                    if (confirmedCount < availableSlots) {
                        request.setStatus(Status.CONFIRMED);
                        confirmedRequests.add(RequestMapper.toRequestDto(request));
                        confirmedCount++;
                    } else {
                        request.setStatus(Status.REJECTED);
                        rejectedRequests.add(RequestMapper.toRequestDto(request));
                    }
                }

                event.setConfirmedRequests(confirmed + confirmedCount);

            }

        } else if (status.equals(Status.REJECTED)) {

            for (Long requestId : userIds) {
                Request request = requestMap.get(requestId);

                if (request != null) {
                    request.setStatus(Status.REJECTED);
                    rejectedRequests.add(RequestMapper.toRequestDto(request));
                }
            }

        } else {
            throw new ConflictException("Недопустимый статус для обновления: " + status);
        }

        requestRepository.saveAll(requests);

        if (!confirmedRequests.isEmpty()) {
            eventRepository.save(event);
        }

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    @Override
    public List<ParticipationRequestDto> getInfoOnParticipation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));
        return requestRepository.findAllByRequester(user).stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequestForParticipation(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (event.getInitiator().equals(user)) {
            throw new ConflictException("инициатор события не может добавить запрос на участие в своём событии.");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("нельзя участвовать в неопубликованном событии.");
        }

        long confirmedRequest = event.getConfirmedRequests();
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequest) {
            throw new ConflictException("у события достигнут лимит запросов на участие");
        }

        Optional<Request> existingRequest = requestRepository.findByEventAndRequester(event, user);
        if (existingRequest.isPresent()) {
            throw new ConflictException("Пользователь уже подал запрос на участие в этом событии.");
        }

        Request request = Request.builder()
                .requester(user)
                .event(event)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
            event.setConfirmedRequests(confirmedRequest + 1);
            eventRepository.save(event);
        }

        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto canceledRequestForParticipation(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Optional<Request> request = requestRepository.findById(requestId);
        if (request.isPresent()) {
            Request update = request.get();
            update.setStatus(Status.CANCELED);
            requestRepository.save(update);
        } else {
            throw new NotFoundException("Такого запроса нет.");
        }
        return RequestMapper.toRequestDto(request.get());
    }
}
