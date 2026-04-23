package ru.practicum.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class NewUserRequest {
    @NotBlank(message = "Электронный адрес должен быть указан")
    @Email(message = "Неверный формат электронного адреса")
    @Size(min = 6, max = 254, message = "Электронный адрес должен содержать от 6 до 254 символов")
    String email;

    @NotBlank(message = "Имя пользователя должно быть указано")
    @Size(min = 2, max = 250, message = "Имя пользователя должно содержать от 2 до 205 символов")
    String name;
}
