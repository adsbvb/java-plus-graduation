package ru.practicum.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;

    String text;

    Long eventId;

    String author;

    @JsonFormat(pattern = Constant.DATE_TIME_FORMAT)
    LocalDateTime create;

    Long like = 0L;
}
