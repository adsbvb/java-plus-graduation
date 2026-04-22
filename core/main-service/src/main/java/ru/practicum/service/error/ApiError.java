package ru.practicum.service.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    // Сведения об ошибке
    List<String> errors;
    String message;
    String reason;
    String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    String timestamp;
}
