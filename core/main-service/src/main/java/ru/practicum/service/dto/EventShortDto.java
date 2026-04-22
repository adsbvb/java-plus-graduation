package ru.practicum.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class EventShortDto {
    //Краткая информация о событии
    Long id;

    String annotation;

    @JsonProperty(value = "category")
    CategoryDto categoryDto;

    Long confirmedRequests;

    @JsonFormat(pattern = Constant.DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    @JsonProperty(value = "initiator")
    UserShortDto initiatorDto;

    Boolean paid;

    String title;

    Long views;
}
