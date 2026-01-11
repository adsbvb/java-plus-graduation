package ru.practicum.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequestDto {
    @NotBlank(message = "Поле text не должно быть пустым.")
    @Size(max = 1000, message = "Комментарий должен быть до 2000 символов.")
    private String text;
}
