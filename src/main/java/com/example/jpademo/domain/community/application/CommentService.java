package com.example.jpademo.domain.community.application;

public interface CommentService {
    void addComment(Long boardId, Long userId, String content);
}
