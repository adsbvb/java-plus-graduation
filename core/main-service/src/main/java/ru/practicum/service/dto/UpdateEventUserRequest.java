package ru.practicum.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.service.dto.enums.UserStateAction;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {
    // Данные для изменения информации о событии. Если поле в запросе не указано (равно null) - значит изменение этих данных не треубется.
    @Size(min = 20, max = 2000, message = "Аннотация должна быть от 20 до 2000 символов.")
    String annotation;

    @JsonProperty(value = "category")
    Long categoryId;

    @Size(min = 20, max = 7000, message = "Описание должно быть от 20 до 7000 символов.")
    String description;

    @FutureOrPresent(message = "Дата события не может быть в прошлом.")
    @JsonFormat(pattern = Constant.DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    @JsonProperty(value = "location")
    LocationDto locationDto;

    Boolean paid;

    @Min(value = 0, message = "Ограничение участников должно быть >= 0.")
    Integer participantLimit;

    Boolean requestModeration;

    UserStateAction stateAction;

    @Size(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов.")
    String title;
}
