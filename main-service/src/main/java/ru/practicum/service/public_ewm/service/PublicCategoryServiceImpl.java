package ru.practicum.service.public_ewm.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatClient;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.service.dal.CategoryRepository;
import ru.practicum.service.dto.CategoryDto;
import ru.practicum.service.dto.Constant;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.mapper.CategoryMapper;
import ru.practicum.service.model.Category;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class PublicCategoryServiceImpl implements PublicCategoryService {
    final CategoryRepository categoryRepository;
    final StatClient statClient;

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
