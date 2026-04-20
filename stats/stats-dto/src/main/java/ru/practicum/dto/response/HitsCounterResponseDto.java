package ru.practicum.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HitsCounterResponseDto {
	/*
	Основной класс, для получения статистики.
	Название сервиса(app),
	uri по которому выгружаем статистику
	Количество обращений
	 */

	String app;

	String uri;

	Long hits;
}
