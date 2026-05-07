package ru.practicum.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.UserShortDto;
import ru.practicum.servcie.UserService;

import java.util.List;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Validated
public class InternalUserController {
    private final UserService userService;

    @GetMapping("/user/{id}/exists")
    public boolean existsUserInternal(
            @PathVariable(name = "id") @Positive Long userId
    ) {
        return userService.existsByIdInternal(userId);
    }

    @GetMapping("/user/{id}")
    UserShortDto getUserByIdInternal(
            @PathVariable(name = "id") Long userId) {
        return userService.getByIdInternal(userId);
    }

    @GetMapping("/by-ids")
    List<UserShortDto> getUsersInternal(
            @RequestParam("userId") List<Long> userIds) {
        return userService.getUsersByIdsInternal(userIds);
    }
}
