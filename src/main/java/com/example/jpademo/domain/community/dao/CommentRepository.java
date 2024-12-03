package com.example.jpademo.domain.community.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.jpademo.domain.community.domain.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
