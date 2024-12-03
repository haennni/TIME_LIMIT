package com.example.jpademo.domain.like.application;

public interface LikeService {
    void addLikeOrRemoveLike(Long boardId, Long userId);
}
