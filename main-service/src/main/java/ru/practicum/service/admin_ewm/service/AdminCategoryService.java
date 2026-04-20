package ru.practicum.service.admin_ewm.service;

import ru.practicum.service.dto.CategoryDto;
import ru.practicum.service.dto.NewCategoryDto;

public interface AdminCategoryService {
    CategoryDto createCategory(NewCategoryDto dto);

    void deleteCategory(Long id);

    CategoryDto updateCategory(Long id, CategoryDto dto);
}
