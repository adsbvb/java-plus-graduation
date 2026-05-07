package ru.practicum.event.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.UserClient;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

@Slf4j
@Component
@RequiredArgsConstructor
public class InternalEventService {
    private final EventRepository eventRepository;
    private final UserClient userClient;

    public EventFullDto getEventById(Long eventId, UserShortDto user) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> {
                    log.warn("Событие на найдено, id: {}", eventId);
                    return new NotFoundException("Событие не найдено, id: " + eventId);
                });

        UserShortDto userTest = userClient.getUserByIdInternal(event.getInitiatorId());
        log.info("Событие найдено: {}", eventId);

        return EventMapper.eventToEventFullDto(event, userTest);
    }

    @Transactional
    public void incrementConfirmedRequests(Long eventId, int count) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> {
                    log.warn("Событие на найдено, id: {}", eventId);
                    return new NotFoundException("Событие не найдено, id: " + eventId);
                });

        long newConfirmed = event.getConfirmedRequests() + count;

        if (event.getParticipantLimit() > 0 && newConfirmed > event.getParticipantLimit()) {
            log.warn("Лимит участников превышен. Лимит: {}. Требование: {}", event.getParticipantLimit(), newConfirmed);
            throw new ConflictException("Лимит участников превышен");
        }

        event.setConfirmedRequests(newConfirmed);
        eventRepository.save(event);
        log.info("Количество подтвержденных пользователей для события id={} изменено. Текущее количество: {}",
                eventId, newConfirmed);
    }
}
