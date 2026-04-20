package ru.practicum.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    // Подборка событий
    @JsonProperty(value = "events")
    Set<Long> eventsIds;    // set - uniqueItems: true

    @Builder.Default
    Boolean pinned = false;

    @NotBlank(message = "Название подборки должно быть пустым")
    @Size(min = 1, max = 50, message = "Название подборки должно содержать от 1 до 50 символов")
    String title;
}
