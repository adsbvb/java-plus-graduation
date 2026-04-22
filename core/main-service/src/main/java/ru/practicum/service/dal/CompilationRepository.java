package ru.practicum.service.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.service.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Query(value = """
            SELECT *
            FROM compilations c
            WHERE (:pinned IS NULL OR c.pinned = :pinned)
            ORDER BY c.id
            LIMIT :size OFFSET :from
            """,
            nativeQuery = true)
    List<Compilation> findCompilations(@Param("pinned") Boolean pinned,
                                       @Param("from") Integer from,
                                       @Param("size") Integer size);

    boolean existsByTitle(String title);
}
