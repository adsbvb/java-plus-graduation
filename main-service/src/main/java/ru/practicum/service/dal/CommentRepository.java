package ru.practicum.service.dal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.service.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByAuthor_Id(Long authorId, Pageable pageable);

    List<Comment> findAllByEvent_Id(Long eventId,  Pageable pageable);

    List<Comment> findAllByEvent_IdAndAuthor_Id(Long eventId, Long authorId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE LOWER(c.text) LIKE LOWER(CONCAT('%', :text, '%')) ORDER BY c.createdOn DESC")
    List<Comment> findAllByText(@Param("text")String text, Pageable pageable);
}
