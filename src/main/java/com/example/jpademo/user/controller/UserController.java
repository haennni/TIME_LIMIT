package com.example.jpademo.user.controller;

import com.example.jpademo.user.domain.User;
import com.example.jpademo.user.dto.RequestLoginDto;
import com.example.jpademo.user.dto.RequestSignUpDto;
import com.example.jpademo.user.application.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입 페이지
    @GetMapping("/signup")
    public String showSignUpForm() {
        return "signup";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signUp(RequestSignUpDto request, Model model) {
        try {
            userService.signUpUser(request);
            model.addAttribute("message", "회원가입 성공!");
            return "login"; // 회원가입 후 로그인 페이지로 이동
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "signup"; // 실패 시 다시 회원가입 페이지로 이동
        }
    }

    @RequestMapping("/login")
    public String login(@RequestParam String userId, HttpSession session) {
        session.setAttribute("userId", userId);
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(RequestLoginDto request, Model model, HttpSession session) {
        try {
            // 유저 인증 (userService에서 로그인 처리)
            User user = userService.login(request);
            // 세션에 사용자 ID 저장
            session.setAttribute("userId", user.getId());
            // 로그인 성공 메시지
            model.addAttribute("message", "로그인 성공!");
            // 로그인 후 게시글 리스트 페이지로 이동
            return "redirect:/home";
        } catch (Exception e) {
            // 에러 메시지 처리
            model.addAttribute("error", e.getMessage());
            return "login"; // 실패 시 로그인 페이지로 다시 이동
        }
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션 무효화
        session.invalidate();
        return "redirect:/login";
    }

}
