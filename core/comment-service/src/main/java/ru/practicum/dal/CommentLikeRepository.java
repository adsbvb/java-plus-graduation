package ru.practicum.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.CommentLike;

import java.util.List;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    List<CommentLike> findByCommentIdIn(List<Long> commentIds);
}
