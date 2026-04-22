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
public class CommentAdminDto {
    Long id;

    String text;

    Long authorId;

    Long eventId;

    @JsonFormat(pattern = Constant.DATE_TIME_FORMAT)
    LocalDateTime createdOn;

    Integer likesCount;
}
