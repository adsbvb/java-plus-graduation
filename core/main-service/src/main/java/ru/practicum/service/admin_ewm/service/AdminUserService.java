package ru.practicum.service.admin_ewm.service;

import ru.practicum.service.dto.NewUserRequest;
import ru.practicum.service.dto.UserDto;

import java.util.List;

public interface AdminUserService {
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    UserDto createUser(NewUserRequest dto);

    void deleteUser(Long id);

}
