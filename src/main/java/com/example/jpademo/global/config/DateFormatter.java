package com.example.jpademo.global.config;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateFormatter {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public String getDateNow(LocalDateTime createTime) {
        LocalDateTime now = LocalDateTime.now();
        return now.format(FORMATTER);
    }

}