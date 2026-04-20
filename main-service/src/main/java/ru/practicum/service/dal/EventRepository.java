package ru.practicum.service.dal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.service.model.Event;
import ru.practicum.service.model.enums.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByIdIn(Set<Long> ids);

    Page<Event> findByInitiator_Id(Long userId, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (e.eventDate >= :rangeStart) " +
            "AND (e.eventDate <= :rangeEnd) " +
            "ORDER BY e.createdOn DESC")
    List<Event> findEventByAdmin(
            @Param("users") List<Long> users,
            @Param("states") List<State> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    @Query("""
        SELECT e
        FROM Event e
        WHERE e.state = ru.practicum.service.model.enums.State.PUBLISHED
          AND (
                :text IS NULL
                OR LOWER(e.annotation) LIKE CONCAT('%', LOWER(CAST(:text AS string)), '%')
                OR LOWER(e.description) LIKE CONCAT('%', LOWER(CAST(:text AS string)), '%')
              )
          AND (:paid IS NULL OR e.paid = :paid)
          AND (:categories IS NULL OR e.category.id IN :categories)
          AND e.eventDate >= :rangeStart
          AND e.eventDate <= :rangeEnd
          AND (:onlyAvailable = FALSE
               OR e.participantLimit = 0
               OR e.confirmedRequests < e.participantLimit)
        ORDER BY e.eventDate ASC
        """)
    List<Event> findPublicEvents(@Param("text") String text,
                                 @Param("categories") List<Long> categories,
                                 @Param("paid") Boolean paid,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 @Param("onlyAvailable") Boolean onlyAvailable,
                                 Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.id = :id AND e.state = 'PUBLISHED'")
    Optional<Event> findPublishedById(@Param("id") Long id);

}
