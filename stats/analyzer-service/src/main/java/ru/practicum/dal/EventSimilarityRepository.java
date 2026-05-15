package ru.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.EventSimilarityEntity;

import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarityEntity, Long> {
    List<EventSimilarityEntity> findByEventAInAndEventBIn(List<Long> eventAs, List<Long> eventBs);

    @Query("SELECT s FROM EventSimilarityEntity s WHERE s.eventA = :eventId OR s.eventB = :eventId")
    List<EventSimilarityEntity> findSimilarEvents(@Param("eventId") Long eventId);

    @Query("SELECT s FROM EventSimilarityEntity s WHERE s.eventA IN :eventIds OR s.eventB IN :eventIds")
    List<EventSimilarityEntity> findSimilarEventsByEventIds(@Param("eventIds") List<Long> eventIds);
}
