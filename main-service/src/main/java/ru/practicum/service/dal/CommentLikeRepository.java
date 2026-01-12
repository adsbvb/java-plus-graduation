package ru.practicum.service.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.service.model.CommentLike;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

}
