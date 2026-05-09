package ru.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository  extends JpaRepository<Request, Long> {
    List<Request> findAllByRequesterId(Long userId);

    Optional<Request> findByEventIdAndRequesterId(Long eventId, Long userId);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByIdIn(List<Long> requestIds);
}
