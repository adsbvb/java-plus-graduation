package ru.practicum.service.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.service.model.Event;
import ru.practicum.service.model.Request;
import ru.practicum.service.model.User;

import java.util.List;
import java.util.Optional;

public interface RequestRepository  extends JpaRepository<Request, Long> {

    List<Request> findAllByEvent(Event eventId);

    List<Request> findAllByRequester(User userId);

    Optional<Request> findByEventAndRequester(Event eventId, User userId);

}
