package ru.practicum.category.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto dto);

    void deleteCategory(Long id);

    CategoryDto updateCategory(Long id, CategoryDto dto);

    List<CategoryDto> getCategories(Integer from, Integer size, HttpServletRequest request);

    CategoryDto getById(Long catId, HttpServletRequest request);
}
