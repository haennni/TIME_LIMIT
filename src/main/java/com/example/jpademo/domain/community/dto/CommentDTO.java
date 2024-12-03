package com.example.jpademo.domain.community.dto;

import lombok.Getter;
import com.example.jpademo.domain.community.domain.Comment;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class CommentDTO {
    private String content;
    private String userName;
    private String commentTime; // String으로 변경

    public CommentDTO(String content, String userName, String commentTime) {
        this.content = content;
        this.userName = userName;
        this.commentTime = commentTime; // 포맷된 시간 전달
    }

    public static CommentDTO toDto(Comment comment) {
        String formattedTime = comment.getCommentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return new CommentDTO(comment.getContent(), comment.getUser().getUserName(), formattedTime);
    }
}
