package ru.practicum.service.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.service.dto.CommentAdminDto;
import ru.practicum.service.dto.CommentDto;
import ru.practicum.service.dto.CommentRequestDto;
import ru.practicum.service.model.Comment;
import ru.practicum.service.model.Event;
import ru.practicum.service.model.User;

@UtilityClass
public class CommentMapper {
    public Comment commentDtoToComment(CommentRequestDto commentRequestDto, User user, Event event) {
        return Comment.builder()
                .text(commentRequestDto.getText())
                .author(user)
                .event(event)
                .build();
    }

    public CommentDto commentToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .eventId(comment.getEvent().getId())
                .create(comment.getCreatedOn())
                .build();
    }

    public CommentAdminDto commentToCommentAdminDto(Comment comment) {
        return CommentAdminDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthor().getId())
                .eventId(comment.getEvent().getId())
                .createdOn(comment.getCreatedOn())
                .likesCount(comment.getLikesCount())
                .build();
    }

    public CommentDto toCommentDtoWithLikes(Comment comment, Integer likes) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .eventId(comment.getEvent().getId())
                .create(comment.getCreatedOn())
                .like(likes)
                .build();
    }
}
