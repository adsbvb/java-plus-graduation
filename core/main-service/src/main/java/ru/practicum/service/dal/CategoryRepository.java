package ru.practicum.service.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.service.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByName(String name);
}
