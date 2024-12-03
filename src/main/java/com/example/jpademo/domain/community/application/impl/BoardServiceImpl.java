package com.example.jpademo.domain.community.application.impl;

import com.example.jpademo.domain.community.dao.BoardRepository;
import com.example.jpademo.domain.community.domain.Board;
import com.example.jpademo.domain.community.dto.BoardDTO;
import com.example.jpademo.domain.community.application.BoardService;
import com.example.jpademo.domain.community.dto.CommentDTO;
import com.example.jpademo.domain.emotion.application.EmotionService;
import com.example.jpademo.domain.like.dao.LikeRepository;
import com.example.jpademo.user.dao.UserRepository;
import com.example.jpademo.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final EmotionService emotionService;

    @Autowired
    public BoardServiceImpl(BoardRepository boardRepository, UserRepository userRepository, LikeRepository likeRepository, EmotionService emotionService) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.emotionService = emotionService;
    }

/*    @Override
    @Transactional(readOnly = true)
    public List<BoardDTO> findAll() {
        return boardRepository.findAll().stream()
                .map(BoardDTO::toDto)
                .collect(Collectors.toList());
    }*/

    @Transactional(readOnly = true)
    public List<BoardDTO> findAll() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return boardRepository.findAll().stream()
                .map(board -> {
                    BoardDTO dto = BoardDTO.toDto(board); // Board를 BoardDTO로 변환
                    dto.setCreateTime(board.getCreateTime().format(formatter)); // 작성일을 문자열로 변환 후 DTO에 설정
                    dto.setComments(
                            board.getComments().stream()
                                    .map(comment -> {
                                        // 댓글 시간 포맷팅
                                        String formattedTime = comment.getCommentTime().format(formatter);
                                        return new CommentDTO(
                                                comment.getContent(),
                                                comment.getUser().getUserName(),
                                                formattedTime // 포맷된 시간 전달
                                        );
                                    })
                                    .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList()); // BoardDTO 리스트로 변환 후 반환
    }


    @Transactional(readOnly = true)
    public BoardDTO findById(Long boardId, Long currentUserId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        BoardDTO dto = BoardDTO.toDto(board);

        if (currentUserId != -1L) {
            User findUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            boolean likedByCurrentUser = likeRepository.findByBoardAndUser(board, user).isPresent();
            dto.setLikedByCurrentUser(likedByCurrentUser); // 좋아요 여부 설정
        } else {
            dto.setLikedByCurrentUser(false); // 비로그인 사용자는 좋아요를 누를 수 없음
        }

        dto.setLikeCount(board.likeCount()); // 좋아요 수 설정


        return dto;
    }

    @Override
    @Transactional
    public void save(BoardDTO boardDTO) {
        Long userId = boardDTO.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Board board = new Board(user, boardDTO);
        boardRepository.save(board);
        if (board.getEmotion().equals("슬픔") || board.getEmotion().equals("분노")) {
            String emotionContent = emotionService.processEmotion(board.getIdx());
            board.createEmotionContent(emotionContent); // 결과를 Board 객체에 설정
            boardRepository.save(board); // 다시 저장
        }
    }

    @Override
    public void update(Long boardId, BoardDTO boardDto) {
        // 기존 게시글 조회
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + boardDto.getIdx()));

        board.update(boardDto);
        boardRepository.save(board);
    }

    @Override
    public void deleteById(long idx) {
        boardRepository.deleteById(idx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardDTO> findByTitle(String search) {
        return boardRepository.findAll().stream()
                .filter(board -> board.getTitle().contains(search))
                .map(BoardDTO::toDto)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public List<BoardDTO> getRecentBoards() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1); // 하루 전
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return boardRepository.findAllByCreateTimeWithinOneDay(oneDayAgo).stream()
                .map(board -> {
                    BoardDTO dto = BoardDTO.toDto(board);
                    dto.setCreateTime(board.getCreateTime().format(formatter)); // 작성일 포맷팅
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /* 감정별 게시글 조회 */
    @Override
    @Transactional(readOnly = true)
    public List<BoardDTO> findByEmotion(String search) {
        return boardRepository.findAll().stream()
                .filter(board -> board.getEmotion().contains(search))
                .map(BoardDTO::toDto)
                .collect(Collectors.toList());
    }

/*    public List<BoardDTO> getRecentBoards() {
        // 하루 전 시간을 계산
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<Board> boards = boardRepository.findAllByCreateTimeWithinOneDay(oneDayAgo);
        return boards.stream()
                .map(BoardDTO::toDto)
                .collect(Collectors.toList());
    }*/
}