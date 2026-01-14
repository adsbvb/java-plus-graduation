package ru.practicum.service.dal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.service.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByAuthorIdOrderByCreatedOnDesc(Long authorId, Pageable pageable);

    List<Comment> findAllByEventIdAndAuthorIdOrderByCreatedOnDesc(Long eventId, Long authorId, Pageable pageable);

    List<Comment> findByEvent_IdOrderByCreatedOnDesc(Long eventId, Pageable pageable);

    List<Comment> findByTextContainingIgnoreCaseOrderByCreatedOnDesc(String text, Pageable pageable);
}
