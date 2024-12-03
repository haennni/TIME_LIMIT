package com.example.jpademo.user.application;

import com.example.jpademo.user.domain.User;
import com.example.jpademo.user.dto.RequestLoginDto;
import com.example.jpademo.user.dto.RequestSignUpDto;
import com.example.jpademo.user.dao.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 회원가입
    public void signUpUser(RequestSignUpDto request) {
        if (userRepository.existsByUserId(request.userId())) {
            throw new IllegalArgumentException("이미 존재하는 사용자 ID입니다.");}
        User user = new User(request.userId(), request.userName(), request.userPwd());
        userRepository.save(user);
    }

    // 로그인
    public User login(RequestLoginDto request) {
        User user = userRepository.findByUserId(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        if (!request.userPwd().equals(user.getUserPwd())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");}
        if (!request.userId().equals(user.getUserId())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");}
        return user;
    }
}