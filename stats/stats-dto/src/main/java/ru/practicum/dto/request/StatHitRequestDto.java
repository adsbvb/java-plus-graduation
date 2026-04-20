package ru.practicum.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatHitRequestDto {
	/*
	Используется при вызове эндпоинта /hit
	Сохранение информации о том, что на uri конкретного сервиса был отправлен запрос пользователем.
	Название сервиса(app), uri и ip пользователя берутся из тела запроса.
	 */

	@NotBlank
	String app;

	@NotBlank
	String uri;

	@NotBlank
	String ip;

	@NotBlank
	String timestamp;
}
