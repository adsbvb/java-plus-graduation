package ru.practicum.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.service.model.enums.State;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    @NotBlank(message = "Поле 'annotation' не может быть пустым")
    String annotation;

    @NotNull(message = "Поле 'category' не может быть null")
    @JsonProperty(value = "category")
    CategoryDto categoryDto;

    Long confirmedRequests;

    @JsonFormat(pattern = Constant.DATE_TIME_FORMAT)
    LocalDateTime createdOn;

    String description;

    @NotNull(message = "Поле 'eventDate' не может быть null")
    @JsonFormat(pattern = Constant.DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    Long id;

    @NotNull(message = "Поле 'initiator' не может быть null")
    @JsonProperty(value = "initiator")
    UserShortDto initiatorDto;

    @NotNull(message = "Поле 'location' не может быть null")
    @JsonProperty(value = "location")
    LocationDto locationDto;

    @NotNull(message = "Поле 'paid' не может быть null")
    Boolean paid;

    Integer participantLimit;

    @JsonFormat(pattern = Constant.DATE_TIME_FORMAT)
    LocalDateTime publishedOn;

    Boolean requestModeration;

    State state;

    @NotBlank(message = "Поле 'title' не может быт пустым")
    String title;

    Long views;
}
