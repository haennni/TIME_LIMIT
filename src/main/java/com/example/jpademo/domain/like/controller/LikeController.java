package com.example.jpademo.domain.like.controller;

import com.example.jpademo.domain.like.application.LikeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/board/{boardId}")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/like")
    public String addOrRemoveLike(@PathVariable Long boardId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        likeService.addLikeOrRemoveLike(boardId, userId);
        return "redirect:/board/" + boardId; // 처리 후 게시글 상세 페이지로 리다이렉트
    }
}