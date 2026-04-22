package ru.practicum.service.admin_ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.service.dal.CategoryRepository;
import ru.practicum.service.dto.CategoryDto;
import ru.practicum.service.dto.NewCategoryDto;
import ru.practicum.service.error.ConflictException;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.mapper.CategoryMapper;
import ru.practicum.service.model.Category;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminCategoryServiceImpl implements AdminCategoryService {
    private final CategoryRepository repository;

    @Override
    public CategoryDto createCategory(NewCategoryDto dto) {
        log.info("Попытка создания новой категории с названием: {}", dto.getName());

        if (repository.existsByName(dto.getName())) {
            log.warn("Ошибка при создании категории: категория с названием '{}' уже существует", dto.getName());
            throw new ConflictException("Категория с названием " + dto.getName() + " уже существует");
        }

        log.debug("Создание сущности Category из DTO: {}", dto);
        Category category = CategoryMapper.toCategoryEntity(dto);

        log.debug("Сохранение категории в базу данных");
        Category saved = repository.save(category);

        log.info("Категория успешно создана: id={}, name={}", saved.getId(), saved.getName());
        return CategoryMapper.toCategoryDto(saved);
    }

    @Override
    public void deleteCategory(Long catId) {
        log.info("Попытка удаления категории с id={}", catId);

        Category category = repository.findById(catId)
                .orElseThrow(() -> {
                    log.error("Категория с id={} не найдена", catId);
                    return new NotFoundException("Категория с id=" + catId + " не найдена");
                });

        log.debug("Проверка связанных событий для категории id={}", catId);
        if (!category.getEvents().isEmpty()) {
            log.warn("Нельзя удалить категорию id={}: связано {} событий", catId, category.getEvents().size());
            throw new ConflictException("Нельзя удалить категорию с id=" + catId + " так как с ней связаны события." +
                    " Количество: " + category.getEvents().size());
        }

        log.debug("Удаление категории id={} из базы данных", catId);
        repository.delete(category);

        log.info("Категория с id={} успешно удалена", catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto dto) {
        log.info("Попытка обновления категории с id={}. Новое название: {}", catId, dto.getName());

        Category category = repository.findById(catId)
                .orElseThrow(() -> {
                    log.error("Категория с id={} не найдена для обновления", catId);
                    return new NotFoundException("Категория с id=" + catId + " не найдена для обновления");
                });

        if (!category.getName().equals(dto.getName()) &&
            repository.existsByNameAndIdNot(dto.getName(), catId)) {
            log.warn("Ошибка при обновлении категории id={}: название '{}' уже используется",
                    catId, dto.getName());
            throw new ConflictException("Категория с названием " + dto.getName() + " уже используется");
        }

        log.info("Изменение названия категории id={} с '{}' на '{}'", catId, category.getName(), dto.getName());
        category.setName(dto.getName());

        log.debug("Сохранение обновленной категории в базу данных");
        Category updated = repository.save(category);

        log.info("Категория с id={} успешно обновлена", catId);
        return CategoryMapper.toCategoryDto(updated);
    }
}
