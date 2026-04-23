package ru.practicum.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCompilationRequest {
    // Изменение информации о подборке событий. Если поле в запросе не указано (равно null) - значит изменение этих данных не требуется.
    @JsonProperty(value = "events")
    Set<Long> eventsIds;    // set - uniqueItems: true

    Boolean pinned;

    @Size(min = 1, max = 50, message = "Название подборки должно содержать от 1 до 50 символов")
    String title;
}
