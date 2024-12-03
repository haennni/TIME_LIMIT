package com.example.jpademo.domain.community.dto;

import com.example.jpademo.domain.community.domain.Board;
import com.example.jpademo.domain.community.domain.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class BoardDTO {
    private long idx;
    private String title;
    private String content;
    private String image;
    private String createTime;
    private Long userId;
    private String userName;
    private int likeCount; // 좋아요 수 추가
    private List<CommentDTO> comments; // 변경된 부분
    private boolean likedByCurrentUser; // 현재 사용자 좋아요 여부 추가

    public BoardDTO(long idx, String title, String content, String image, String createTime, String userName, int likeCount, Long userId, List<CommentDTO> comments) {
        this.idx = idx;
        this.title = title;
        this.content = content;
        this.image = image;
        this.createTime = createTime;
        this.userName = userName;
        this.likeCount = likeCount;
        this.userId = userId;
        this.comments = comments;
    }

    public static BoardDTO toDto(Board board) {
        // 사용자 이름 처리 (null 방지)
        String userName = (board.getUser() != null) ? board.getUser().getUserName() : "Unknown";

        // 사용자 ID 처리 (null 방지)
        Long userId = (board.getUser() != null) ? board.getUser().getId() : null;

        List<CommentDTO> comments = (board.getComments() != null)
                ? board.getComments().stream()
                .map(comment -> {
                    String formattedTime = comment.getCommentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    return new CommentDTO(comment.getContent(), comment.getUser().getUserName(), formattedTime);
                })
                .collect(Collectors.toList())
                : List.of();

        // DTO 생성 및 반환
        return new BoardDTO(
                board.getIdx(),
                board.getTitle(),
                board.getContent(),
                board.getImage(),
                String.valueOf(board.getCreateTime()), // 작성일을 문자열로 변환
                userName,
                board.likeCount(), // 좋아요 수 설정
                userId,
                comments
        );
    }
}