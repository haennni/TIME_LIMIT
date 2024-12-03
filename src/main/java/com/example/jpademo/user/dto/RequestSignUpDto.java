package com.example.jpademo.user.dto;

import com.example.jpademo.user.domain.User;

public record RequestSignUpDto(
        String userId,
        String userName,
        String userPwd
) {
    public  RequestSignUpDto toDto(User user) {
        return new  RequestSignUpDto(user.getUserId(), user.getUserName(), user.getUserPwd());
    }
}
