package ru.practicum.service.admin_ewm.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminEventParam {

    private List<Long> users;
    private List<String> states;
    private List<Long> categories;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    LocalDateTime rangeStart = LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0, 0);

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    LocalDateTime rangeEnd =  LocalDateTime.of(2099, Month.DECEMBER, 31, 23, 59, 59);

    @Min(value = 0, message = "Параметр 'from' не может быть отрицательным")
    @Builder.Default
    private Integer from = 0;

    @Positive(message = "Параметр 'size' должен быть положительным числом")
    @Builder.Default
    private Integer size = 10;

    @AssertTrue(message = "Дата начала не может быть позже даты окончания")
    public boolean isValidDateRange() {
        if (rangeStart == null || rangeEnd == null) {
            return true;
        }
        return !rangeStart.isAfter(rangeEnd);
    }
}
