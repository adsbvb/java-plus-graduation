package ru.practicum.servcie;

import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    UserDto createUser(NewUserRequest dto);

    void deleteUser(Long id);

    boolean existsByIdInternal(Long userId);

    UserShortDto getByIdInternal(Long userId);

    List<UserShortDto> getUsersByIdsInternal(List<Long> userIds);
}
