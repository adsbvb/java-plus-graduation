package ru.practicum.service.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.service.dto.*;
import ru.practicum.service.dto.enums.UserStateAction;
import ru.practicum.service.model.Category;
import ru.practicum.service.model.Event;
import ru.practicum.service.model.User;
import ru.practicum.service.model.enums.State;

import java.util.Optional;

@UtilityClass
public class EventMapper {
    public EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .categoryDto(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiatorDto(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .build();
    }

    public EventShortDto toEventShortDto(Event event, Long views) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .categoryDto(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiatorDto(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public Event newEventDtoToEvent(NewEventDto newEventDto, User user, Category category) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .initiator(user)
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(LocationMapper.locationDtoToLocation(newEventDto.getLocationDto()))
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .build();
    }

    public EventFullDto eventToEventFullDto(Event event) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .categoryDto(CategoryMapper.toCategoryDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .locationDto(LocationMapper.locationToLocationDto(event.getLocation()))
                .initiatorDto(UserMapper.toUserShortDto(event.getInitiator()))
                .state(event.getState())
                .id(event.getId())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .title(event.getTitle())
                .build();
    }

    public Event updateEventDtoToEvent(Event event, UpdateEventUserRequest update, Optional<Category> category) {
        Optional.ofNullable(update.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(update.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(update.getTitle()).ifPresent(event::setTitle);
        Optional.ofNullable(update.getEventDate()).ifPresent(event::setEventDate);
        Optional.ofNullable(update.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(update.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(update.getRequestModeration()).ifPresent(event::setRequestModeration);
        category.ifPresent(event::setCategory);
        LocationDto locationDto = update.getLocationDto();
        if (locationDto != null) {
            event.setLocation(LocationMapper.locationDtoToLocation(locationDto));
        }
        UserStateAction state = update.getStateAction();
        if (UserStateAction.CANCEL_REVIEW.equals(state)) {
            event.setState(State.CANCELED);
        }
        if (UserStateAction.SEND_TO_REVIEW.equals(state)) {
            event.setState(State.PENDING);
        }
        if (state == null) {
            event.setState(State.PENDING);
        }

        return event;
    }

    public EventFullDto toEventFullDto(Event event, Long views) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .categoryDto(CategoryMapper.toCategoryDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .locationDto(LocationMapper.locationToLocationDto(event.getLocation()))
                .initiatorDto(UserMapper.toUserShortDto(event.getInitiator()))
                .state(event.getState())
                .id(event.getId())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static Long extractIdFromUri(String uri) {
        String[] parts = uri.split("/");
        String id = parts[parts.length - 1];

        return Long.parseLong(id);
    }
}
