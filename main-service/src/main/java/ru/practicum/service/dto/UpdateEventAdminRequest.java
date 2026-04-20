package ru.practicum.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.service.dto.enums.AdminStateAction;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
    // Данные для изменения информации о событии. Если поле в запросе не указано (равно null) - значит изменение этих данных не требуется.
    @Size(min = 20, max = 2000)
    String annotation;

    @JsonProperty(value = "category")
    Long categoryId;

    @Size(min = 20, max = 7000)
    String description;

    @JsonFormat(pattern = Constant.DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    @JsonProperty(value = "location")
    LocationDto locationDto;

    Boolean paid;

    Integer participantLimit;

    Boolean requestModeration;

    AdminStateAction stateAction;

    @Size(min = 3, max = 120)
    String title;

    @AssertTrue(message = "Дата начала события должна быть не ранее чем за час от даты публикации")
    public boolean isValidEventDate() {
        if (eventDate == null) {
            return true;
        }
        return !eventDate.isBefore(LocalDateTime.now().plusHours(1));
    }
}
