package com.example.jpademo.domain.community.dao;

import com.example.jpademo.domain.community.domain.Board;
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

    @Query("SELECT b FROM Board b WHERE b.createTime >= :threshold")
    List<Board> findAllByCreateTimeWithinOneDay(@Param("threshold") LocalDateTime threshold);

    @EntityGraph(attributePaths = {"user", "comments"})
    Optional<Board> findWithCommentsAndUserByIdx(Long idx);

    @EntityGraph(attributePaths = {"user", "comments"})
    List<Board> findAll();

}
