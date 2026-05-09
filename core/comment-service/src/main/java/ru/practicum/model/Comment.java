package ru.practicum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @NotNull
    @Column(name = "event_id")
    private Long eventId;

    @NotNull
    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "created_on")
    @Builder.Default
    LocalDateTime createdOn  = LocalDateTime.now();

    @Column(name = "likes_count")
    @Builder.Default
    private Integer likesCount = 0;
}
