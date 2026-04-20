package ru.practicum.service.admin_ewm.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.service.admin_ewm.dto.AdminEventParam;
import ru.practicum.service.statistics.StatisticsService;
import ru.practicum.service.dal.CategoryRepository;
import ru.practicum.service.dal.EventRepository;
import ru.practicum.service.dto.EventFullDto;
import ru.practicum.service.dto.UpdateEventAdminRequest;
import ru.practicum.service.dto.enums.AdminStateAction;
import ru.practicum.service.error.ConflictException;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.error.ValidationException;
import ru.practicum.service.mapper.EventMapper;
import ru.practicum.service.model.Category;
import ru.practicum.service.model.Event;
import ru.practicum.service.model.Location;
import ru.practicum.service.model.enums.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class AdminEventServiceImpl implements AdminEventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final StatisticsService  statisticsService;

    private static final String URI_EVENT_ENDPOINT = "/events/";

    @Override
    public List<EventFullDto> getFullEvents(AdminEventParam params) {
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

        List<String> uris = events.stream()
                .map(event -> URI_EVENT_ENDPOINT + event.getId())
                .toList();

        Map<String, Long> eventHits = statisticsService.getViewsByUris(uris, false);

        return events.stream()
                .map(event -> {
                    Long views = eventHits.getOrDefault(URI_EVENT_ENDPOINT + event.getId(), 0L);
                    return EventMapper.toEventFullDto(event, views);
                })
                .toList();
    }

    private List<State> convertStatesEnum(List<String> states) {
        if (states == null || states.isEmpty()) {
            return null;
        }
        return states.stream()
                .map(state -> {
                    try {
                        return State.valueOf(state.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new ValidationException("Некорректное состояние события: " + state);
                    }
                })
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено."));

        if (request.getStateAction() != null) {

            if (request.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
                if (event.getState() != State.PENDING) {
                    throw new ConflictException("Cобытие можно публиковать, только если оно в состоянии ожидания публикации");
                }
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());

            } else if (request.getStateAction() == AdminStateAction.REJECT_EVENT) {
                if (event.getState() == State.PUBLISHED) {
                    throw new ConflictException("Cобытие можно отклонить, только если оно еще не опубликовано");
                }
                event.setState(State.CANCELED);
            }
        }

        updateEventFields(event, request);
        Event updatedEvent = eventRepository.save(event);

        String uri = URI_EVENT_ENDPOINT + event.getId();
        Long views = statisticsService.getViewsByUri(uri,false);

        return EventMapper.toEventFullDto(updatedEvent, views);
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
            event.setLocation(
                    Location.builder()
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
}
