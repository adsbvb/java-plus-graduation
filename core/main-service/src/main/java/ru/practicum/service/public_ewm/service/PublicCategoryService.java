package ru.practicum.service.public_ewm.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.service.dto.CategoryDto;

import java.util.List;

public interface PublicCategoryService {
    List<CategoryDto> getCategories(Integer from, Integer size, HttpServletRequest request);

    CategoryDto getById(Long catId, HttpServletRequest request);
}
