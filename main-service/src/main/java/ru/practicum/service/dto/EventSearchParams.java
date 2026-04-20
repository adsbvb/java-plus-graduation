package ru.practicum.service.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventSearchParams {
	String text;

	List<Long> categories;

	Boolean paid;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime rangeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime rangeEnd;

	Boolean onlyAvailable = false;

	EventSort sort;

	@Min(value = 0, message = "Параметр 'from' не может быть отрицательным")
	Integer from = 0;

	@Min(value = 1, message = "Параметр 'size' должен быть не меньше 1")
	Integer size = 10;

	@AssertTrue(message = "rangeStart must be before or equal to rangeEnd")
	public boolean isRangeValid() {
		if (rangeStart != null && rangeEnd != null) {
			return !rangeStart.isAfter(rangeEnd);
		}

		return true;
	}
}
