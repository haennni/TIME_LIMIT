package com.example.jpademo.domain.like.dao;

import com.example.jpademo.domain.community.domain.Board;
import com.example.jpademo.domain.like.domain.Like;
import com.example.jpademo.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.xml.stream.events.Comment;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByBoardAndUser(Board board, User user); // 반환 타입을 Optional로 설정

}
