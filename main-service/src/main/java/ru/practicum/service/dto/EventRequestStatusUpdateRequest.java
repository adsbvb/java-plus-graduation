package ru.practicum.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.service.model.enums.Status;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateRequest {
    // Изменение статуса запроса на участие в событии текущего пользователя
    @NotNull
    List<Long> requestIds;

    @NotNull
    Status status;
}
