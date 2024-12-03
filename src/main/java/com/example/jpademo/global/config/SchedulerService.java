
/*
package com.example.jpademo;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public class SchedulerService {

    BoardRepository boardRepository;

    @Transactional*/
    //@Scheduled(cron = "*/10 * * * * ?") // 매 10초마다 실행
/*
public void deleteInactiveUsersTest() {
        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        List<User> usersToDelete = userRepository.findByIsActiveFalseAndDeactivatedAtBefore(tenSecondsAgo);
        log.info("삭제할 사용자 수: {}", usersToDelete.size());
        userRepository.deleteByIsActiveFalseAndDeactivatedAtBefore(tenSecondsAgo);
    }
    */