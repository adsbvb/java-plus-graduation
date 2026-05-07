package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.CommentAdminDto;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentRequestDto;
import ru.practicum.model.Comment;

@UtilityClass
public class CommentMapper {
    public Comment commentDtoToComment(CommentRequestDto commentRequestDto, Long userId, Long eventId) {
        return Comment.builder()
                .text(commentRequestDto.getText())
                .authorId(userId)
                .eventId(eventId)
                .build();
    }

    public CommentDto commentToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthorId())
                .eventId(comment.getEventId())
                .create(comment.getCreatedOn())
                .build();
    }

    public CommentAdminDto commentToCommentAdminDto(Comment comment) {
        return CommentAdminDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthorId())
                .eventId(comment.getEventId())
                .createdOn(comment.getCreatedOn())
                .likesCount(comment.getLikesCount())
                .build();
    }

    public CommentDto toCommentDtoWithLikes(Comment comment, Integer likes) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthorId())
                .eventId(comment.getEventId())
                .create(comment.getCreatedOn())
                .like(likes)
                .build();
    }
}
