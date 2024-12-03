package com.example.jpademo.user.dto;

import com.example.jpademo.user.domain.User;

public record RequestLoginDto(
        String userId,
        String userPwd
) {
    public  RequestLoginDto toDto(User user) {
        return new  RequestLoginDto(user.getUserId(), user.getUserPwd());
    }
}
