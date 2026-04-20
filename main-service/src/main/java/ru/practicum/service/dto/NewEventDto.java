package ru.practicum.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    // Новое событие
    @NotBlank(message = "Аннотация не должна быть пустой.")
    @Size(min = 20, max = 2000, message = "Аннотация должна быть от 20 до 2000 символов.")
    String annotation;

    @JsonProperty(value = "category")
    Long categoryId;

    @NotBlank(message = "Описание не может быть пустым.")
    @Size(min = 20, max = 7000, message = "Описание должно быть от 20 до 7000 символов.")
    String description;

    @NotNull(message = "Дата события не может быть пустой.")
    @JsonFormat(pattern = Constant.DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    @NotNull(message = "Локация не может быть пустой.")
    @JsonProperty(value = "location")
    LocationDto locationDto;

    Boolean paid = false;

    @Min(0)
    Integer participantLimit = 0;

    Boolean requestModeration = true;

    @NotBlank
    @Size(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов.")
    String title;
}
