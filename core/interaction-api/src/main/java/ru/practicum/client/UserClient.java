package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserClient {

    @GetMapping("/user/{id}/exists")
    boolean existsUserInternal(
            @PathVariable(name = "id") Long userId);

    @GetMapping("/user/{id}")
    UserShortDto getUserByIdInternal(
            @PathVariable(name = "id") Long userId);

    @GetMapping("/by-ids")
    List<UserShortDto> getUsersInternal(
            @RequestParam("userId") List<Long> userIds);
}
