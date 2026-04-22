package ru.practicum.service.admin_ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.service.dal.UserRepository;
import ru.practicum.service.dto.NewUserRequest;
import ru.practicum.service.dto.UserDto;
import ru.practicum.service.error.ConflictException;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.mapper.UserMapper;
import ru.practicum.service.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {
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
}
