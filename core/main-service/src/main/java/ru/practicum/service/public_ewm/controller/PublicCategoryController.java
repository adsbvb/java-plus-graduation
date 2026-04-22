package ru.practicum.service.public_ewm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.dto.CategoryDto;
import ru.practicum.service.public_ewm.service.PublicCategoryService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/categories")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class PublicCategoryController {
    final PublicCategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategoryByParam(@RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(value = "size", defaultValue = "10") @Positive Integer size,
                                                HttpServletRequest request) {
        log.info("PublicCategoryController: вызов эндпоинта GET /category с параметрами -- from:{}, size:{}", from, size);

        return categoryService.getCategories(from, size, request);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable(value = "catId") Long catId, HttpServletRequest request) {
        log.info("PublicCategoryController: вызов эндпоинта GET /category/{}", catId);

        return categoryService.getById(catId, request);
    }
}
