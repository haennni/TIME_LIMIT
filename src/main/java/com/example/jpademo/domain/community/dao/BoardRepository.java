package com.example.jpademo.domain.community.dao;

import com.example.jpademo.domain.community.domain.Board;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query("SELECT b FROM Board b WHERE b.lifeTime >= :threshold")
    List<Board> findAllByLifeTimeWithinOneDay(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT b FROM Board b ORDER BY SIZE(b.likes) DESC")
    List<Board> findTop10ByLikesSizeDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "comments"})
    List<Board> findAll();

    @Query("SELECT b FROM Board b WHERE b.lifeTime BETWEEN :startTime AND :endTime")
    List<Board> findAllByLifeTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
  
    long countByEmotion(String emotion);

    // 감정별 게시글 수 카운트 (하루 남은 게시글 기준)
    @Query("SELECT b.emotion, COUNT(b) FROM Board b WHERE b.lifeTime >= :threshold GROUP BY b.emotion")
    List<Object[]> countEmotionByLifeTime(@Param("threshold") LocalDateTime threshold);
}
