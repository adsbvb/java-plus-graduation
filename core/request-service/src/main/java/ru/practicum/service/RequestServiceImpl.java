package ru.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dal.RequestRepository;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.enums.State;
import ru.practicum.enums.Status;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Request;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public List<ParticipationRequestDto> getInfoOnParticipation(Long userId) {
        log.info("Получение информации о заявках текущего пользователя id={} на участие в чужих событиях", userId);

        if (!existUser(userId)) {
            throw new NotFoundException("Пользователь не найден, id: " + userId);
        }

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequestForParticipation(Long userId, Long eventId) {
        log.info("Добавление запроса от текущего пользователя id={} на участие в событии id={}", userId, eventId);

        UserShortDto user = getUserById(userId);
        EventFullDto event = getEventById(eventId, user);

        if (event.getInitiatorDto().getId().equals(userId)) {
            log.warn("Инициатор события не может добавить запрос на участие в своём событии");
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            log.warn("Нельзя участвовать в неопубликованном событии. Текущий статус: {}", event.getState());
            throw new ConflictException("Нельзя участвовать в неопубликованном событии. Текущий статус: "
                    + event.getState());
        }

        long confirmedRequest = event.getConfirmedRequests();
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequest) {
            log.warn("У события достигнут лимит запросов на участие");
            throw new ConflictException("У события достигнут лимит запросов на участие");
        }

        Optional<Request> existingRequest = requestRepository.findByEventIdAndRequesterId(eventId, userId);
        if (existingRequest.isPresent()) {
            log.warn("Пользователь уже подал запрос на участие в этом событии");
            throw new ConflictException("Пользователь уже подал запрос на участие в этом событии");
        }
        Status initialStatus = Status.PENDING;

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            initialStatus = Status.CONFIRMED;
            try {
                eventClient.incrementConfirmedRequestsInternal(eventId, 1);
            } catch (FeignException ex) {
                log.error(ex.getMessage());
                throw new ServiceException("Ошибка при вызове сервиса пользователей", ex);
            }
        }

        Request request = Request.builder()
                .requesterId(userId)
                .eventId(eventId)
                .status(initialStatus)
                .build();

        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto canceledRequestForParticipation(Long userId, Long requestId) {
        log.info("Отмена своего запроса пользователя id={} на участие в событии id={}", userId, requestId);

        if (!existUser(userId)) {
            log.warn("Пользователь не найден, id: {}", userId);
            throw new NotFoundException("Пользователь не найден, id: " + userId);
        }

        Optional<Request> request = requestRepository.findById(requestId);
        if (request.isPresent()) {
            Request update = request.get();
            update.setStatus(Status.CANCELED);
            requestRepository.save(update);
            log.info("Статус запроса id={} изменен. Текущий статус: {}", requestId, update.getStatus());
        } else {
            log.warn("Запрос не найден, id: {}", requestId);
            throw new NotFoundException("Такого запроса нет, id: " + requestId);
        }

        return RequestMapper.toRequestDto(request.get());
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEventId(Long eventId) {
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        if (requests.isEmpty()) {
            return List.of();
        }

        return requests.stream().map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return requestRepository.findAllByIdIn(ids).stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> batchUpdateRequests(List<ParticipationRequestDto> requests) {
        List<Request> updateRequests = requests.stream()
                .map(RequestMapper::toRequest)
                .toList();

        return requestRepository.saveAll(updateRequests).stream()
                .map(RequestMapper::toRequestDto)
                .toList();
    }

    private boolean existUser(Long userId) {
        try {
            return userClient.existsUserInternal(userId);
        } catch (FeignException ex) {
            log.error(ex.getMessage());
            throw new ServiceException("Ошибка при вызове сервиса пользователей", ex);
        }
    }

    private EventFullDto getEventById(Long eventId, UserShortDto user) {
        try {
            return eventClient.getEventInternal(eventId, user);
        } catch (FeignException.NotFound ex) {
            log.warn(ex.getMessage());
            throw new NotFoundException("Событие не найдено, id: " + eventId);
        } catch (FeignException ex) {
            log.error(ex.getMessage());
            throw new ServiceException("Ошибка при вызове сервиса событий", ex);
        }
    }

    private UserShortDto getUserById(Long userId) {
        try {
            return userClient.getUserByIdInternal(userId);
        } catch (FeignException.NotFound ex) {
            log.warn(ex.getMessage());
            throw new NotFoundException("Пользователь не найден, id: " + userId);
        } catch (FeignException ex) {
            log.error(ex.getMessage());
            throw new ServiceException("Ошибка при вызове сервиса пользователей", ex);
        }
    }
}
