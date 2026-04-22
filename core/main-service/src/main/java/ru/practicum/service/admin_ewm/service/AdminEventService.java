package ru.practicum.service.admin_ewm.service;

import ru.practicum.service.admin_ewm.dto.AdminEventParam;
import ru.practicum.service.dto.EventFullDto;
import ru.practicum.service.dto.UpdateEventAdminRequest;

import java.util.List;

public interface AdminEventService {
    List<EventFullDto> getFullEvents(AdminEventParam params);

    EventFullDto updateEventByAdmin(Long id, UpdateEventAdminRequest request);
}