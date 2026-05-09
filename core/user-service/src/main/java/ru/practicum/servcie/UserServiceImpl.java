package ru.practicum.servcie;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dal.UserRepository;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);

        Page<User> users;

        if (ids == null || ids.isEmpty()) {
            users = repository.findAll(pageable);
        } else {
            users = repository.findByIdIn(ids, pageable);
        }

        return users.getContent().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Адрес электронной почты уже существует.");
        }

        User user = UserMapper.toUserEntity(dto);
        User savedUser = repository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден.");
        }
        repository.deleteById(id);
    }

    @Override
    public boolean existsByIdInternal(Long userId) {
        return repository.existsById(userId);
    }

    @Override
    public UserShortDto getByIdInternal(Long userId) {
        User user = repository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден, id: " + userId)
        );

        return UserMapper.toUserShortDto(user);
    }

    @Override
    public List<UserShortDto> getUsersByIdsInternal(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        List<User> users = repository.findByIdIn(userIds);

        if (users.isEmpty()) {
            log.warn("Ни один пользователь не найден для списка ids: {}", userIds);
            return List.of();
        }

        log.debug("Найдено {} пользователей для списка ids: {}", users.size(), userIds);
        return users.stream()
                .map(UserMapper::toUserShortDto)
                .toList();
    }
}
