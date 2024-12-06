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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            dto.setLikedByCurrentUser(likedByCurrentUser);
        } else {
            dto.setLikedByCurrentUser(false);
        }
        dto.setLikeCount(board.likeCount());


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
            board.createEmotionContent(emotionContent);
            boardRepository.save(board);
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

    @Transactional(readOnly = true)
    public List<BoardDTO> getRecentBoards() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        return boardRepository.findAllByLifeTimeWithinOneDay(oneDayAgo).stream()
                .map(board -> {
                    BoardDTO dto = BoardDTO.toDto(board);
                    dto.setCreateTime(board.getCreateTime().format(formatter)); // 작성일 포맷팅
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override

    @Transactional(readOnly = true)
    public List<BoardDTO> getPostsWithin23To24Hours() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusDays(1); // 24시간 전
        LocalDateTime endTime = now.minusHours(23); // 23시간 전

        return boardRepository.findAllByLifeTimeBetween(startTime, endTime).stream()
                .map(board -> {
                    BoardDTO dto = BoardDTO.toDto(board);
                    dto.setCreateTime(board.getCreateTime().format(formatter)); // 작성일 포맷팅
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BoardDTO> getPopularBoards() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Board> popularBoards = boardRepository.findTop10ByLikesSizeDesc(pageable);

        return popularBoards.stream()
                .map(BoardDTO::toDto)
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

    @Override
    public long getHappyCount() {
        return boardRepository.countByEmotion("기쁨");
    }

    @Override
    public long getSadCount() {
        return boardRepository.countByEmotion("슬픔");
    }

    @Override
    public long getAngryCount() {
        return boardRepository.countByEmotion("분노");
    }


    /* 하루남은 게시글 감정 조회 */
    @Transactional(readOnly = true)
    public List<Long> getEmotionCountsForRecentBoards() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        // 감정별 게시글 수 카운트를 위한 쿼리 호출
        List<Object[]> emotionCounts = boardRepository.countEmotionByLifeTime(oneDayAgo);

        // 각 감정의 카운트를 저장할 변수 초기화
        long happyCount = 0;
        long sadCount = 0;
        long angryCount = 0;

        // 결과 처리
        for (Object[] result : emotionCounts) {
            String emotion = (String) result[0];  // 감정 값 (기쁨, 슬픔, 분노 등)
            Long count = (Long) result[1];        // 해당 감정의 게시글 수

            // 각 감정에 대한 카운트 처리
            if ("기쁨".equals(emotion)) {
                happyCount = count;
            } else if ("슬픔".equals(emotion)) {
                sadCount = count;
            } else if ("분노".equals(emotion)) {
                angryCount = count;
            }
        }

        return List.of(happyCount, sadCount, angryCount);
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