package ru.practicum.service.admin_ewm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.admin_ewm.service.AdminCategoryService;
import ru.practicum.service.dto.CategoryDto;
import ru.practicum.service.dto.NewCategoryDto;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCategoryController {
    private final AdminCategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto newCategory(
            @RequestBody @Valid NewCategoryDto dto
    ) {
        log.info("POST admin/categories: {}", dto);
        return service.createCategory(dto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @PathVariable(name = "catId") @Positive Long catId
    ) {
        log.info("DELETE admin/categories/{}", catId);
        service.deleteCategory(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(
            @PathVariable(name = "catId") @Positive Long catId,
            @RequestBody @Valid CategoryDto dto
    ) {
        log.info("PATCH admin/categories/{}: {}", catId, dto);
        return service.updateCategory(catId, dto);
    }
}
