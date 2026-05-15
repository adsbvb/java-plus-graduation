package ru.practicum.category.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.stats_server.StatClient;
import ru.practicum.dto.Constant;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.category.dal.CategoryRepository;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final StatClient statClient;

    // Admin

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto dto) {
        log.info("Попытка создания новой категории с названием: {}", dto.getName());

        if (categoryRepository.existsByName(dto.getName())) {
            log.warn("Ошибка при создании категории: категория с названием '{}' уже существует", dto.getName());
            throw new ConflictException("Категория с названием " + dto.getName() + " уже существует");
        }

        log.debug("Создание сущности Category из DTO: {}", dto);
        Category category = CategoryMapper.toCategoryEntity(dto);

        log.debug("Сохранение категории в базу данных");
        Category saved = categoryRepository.save(category);

        log.info("Категория успешно создана: id={}, name={}", saved.getId(), saved.getName());
        return CategoryMapper.toCategoryDto(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        log.info("Попытка удаления категории с id={}", catId);

        Category category = categoryRepository.findById(catId)
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
        categoryRepository.delete(category);

        log.info("Категория с id={} успешно удалена", catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto dto) {
        log.info("Попытка обновления категории с id={}. Новое название: {}", catId, dto.getName());

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> {
                    log.error("Категория с id={} не найдена для обновления", catId);
                    return new NotFoundException("Категория с id=" + catId + " не найдена для обновления");
                });

        if (!category.getName().equals(dto.getName()) &&
            categoryRepository.existsByNameAndIdNot(dto.getName(), catId)) {
            log.warn("Ошибка при обновлении категории id={}: название '{}' уже используется",
                    catId, dto.getName());
            throw new ConflictException("Категория с названием " + dto.getName() + " уже используется");
        }

        log.info("Изменение названия категории id={} с '{}' на '{}'", catId, category.getName(), dto.getName());
        category.setName(dto.getName());

        log.debug("Сохранение обновленной категории в базу данных");
        Category updated = categoryRepository.save(category);

        log.info("Категория с id={} успешно обновлена", catId);
        return CategoryMapper.toCategoryDto(updated);
    }

    // Public

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size, HttpServletRequest request) {
        log.info("PublicCategoryService: выгрузка категорий по заданным параметрам:");
        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categoryList = categoryRepository.findAll(pageable).getContent();
        log.info("{}", categoryList);

        statClient.hit(new StatHitRequestDto(Constant.SERVICE_POSTFIX,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)))
        );

        return categoryList.stream().map(CategoryMapper::toCategoryDto).toList();
    }

    @Override
    public CategoryDto getById(Long catId, HttpServletRequest request) {
        log.info("PublicCategoryService: поиск категории с переданным id:");
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Категория с id: %d не найдена", catId)));
        log.info("{}", category);

        statClient.hit(new StatHitRequestDto(Constant.SERVICE_POSTFIX,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)))
        );

        return CategoryMapper.toCategoryDto(category);
    }
}
