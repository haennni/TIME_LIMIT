package com.example.jpademo.domain.community.domain;

import com.example.jpademo.domain.community.dto.BoardDTO;
import com.example.jpademo.domain.like.domain.Like;
import com.example.jpademo.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idx;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    private String image;

    private LocalDateTime createTime;

    @Column(columnDefinition = "TEXT")
    private String emotionContent;

    @Column(nullable = false)
    private String emotion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;



    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "board", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Column(nullable = false)
    private int likeCount;

    public Board(User user, BoardDTO boardDTO) {
        this.title = boardDTO.getTitle();
        this.content = boardDTO.getContent();
        this.image = boardDTO.getImage();
        this.createTime = LocalDateTime.now();
        this.emotion = boardDTO.getEmotion(); // 추가
        this.user = user;
    }

    public void update(BoardDTO boardDTO) {
        this.title = boardDTO.getTitle();
        this.content = boardDTO.getContent();
        this.image = boardDTO.getImage();
        this.emotion = boardDTO.getEmotion();
    }

    public void extendExpirationTime() {
        createTime.plusHours(1);
    }

    public int likeCount() {
        return this.likes.size();
    }

    public void createEmotionContent(String emotionContent) {
        this.emotionContent = emotionContent;}

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}