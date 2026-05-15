package ru.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.UserActionEntity;

import java.util.List;
import java.util.Set;

public interface UserActionRepository extends JpaRepository<UserActionEntity, Long> {
    List<UserActionEntity> findByUserIdInAndEventIdIn(List<Long> userIds, List<Long> eventIds);

    @Query("SELECT u.eventId FROM UserActionEntity u WHERE u.userId = :userId")
    Set<Long> findUserInteractedEvents(@Param("userId") Long userId);

    @Query("SELECT u FROM UserActionEntity u WHERE u.userId = :userId ORDER BY u.lastActionTime DESC")
    List<UserActionEntity> findRecentActionsByUserId(@Param("userId") Long userId);

    @Query("SELECT u.eventId, SUM(u.weight) FROM UserActionEntity u WHERE u.eventId IN :eventIds GROUP BY u.eventId")
    List<Object[]> getTotalWeightForEvents(@Param("eventIds") List<Long> eventIds);
}
