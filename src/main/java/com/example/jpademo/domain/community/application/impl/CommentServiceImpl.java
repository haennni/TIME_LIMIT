package com.example.jpademo.domain.community.application.impl;

import com.example.jpademo.domain.community.application.CommentService;
import com.example.jpademo.domain.community.dao.BoardRepository;
import com.example.jpademo.domain.community.dao.CommentRepository;
import com.example.jpademo.domain.community.domain.Board;
import com.example.jpademo.domain.community.domain.Comment;
import com.example.jpademo.user.dao.UserRepository;
import com.example.jpademo.user.domain.User;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository, BoardRepository boardRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    public void addComment(Long boardId, Long userId, String content) {
        Board board = existsBoard(boardId);
        User user = existsUser(userId);

        Comment comment = new Comment(content, board, user, LocalDateTime.now());
        commentRepository.save(comment);

        board.extendExpirationTime();
        boardRepository.save(board);
    }

    private User existsUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));}

    private Board existsBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));}
}
