package com.example.jpademo.domain.like.application.impl;

import com.example.jpademo.domain.community.dao.BoardRepository;
import com.example.jpademo.domain.community.domain.Board;
import com.example.jpademo.domain.like.application.LikeService;
import com.example.jpademo.domain.like.dao.LikeRepository;
import com.example.jpademo.domain.like.domain.Like;
import com.example.jpademo.user.dao.UserRepository;
import com.example.jpademo.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public LikeServiceImpl(LikeRepository likeRepository, BoardRepository boardRepository, UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addLikeOrRemoveLike(Long boardId, Long userId) {
        Board board = existsBoard(boardId);
        User user = existsUser(userId);

        // 특정 사용자와 게시글에 대한 좋아요 여부 확인
        likeRepository.findByBoardAndUser(board, user)
                .ifPresentOrElse(
                        likeRepository::delete, // 좋아요가 이미 존재하면 삭제
                        () -> likeRepository.save(new Like(board, user, LocalDateTime.now())) // 존재하지 않으면 추가
                );
    }

    private User existsUser(Long userId) {
        return userRepository.findById(userId)
                 .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));}

    private Board existsBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));}
}
