package ru.practicum.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;

    String text;

    Long eventId;

    String authorName;

    @JsonFormat(pattern = Constant.DATE_TIME_FORMAT)
    LocalDateTime create;

    @Builder.Default
    Integer like = 0;
}
