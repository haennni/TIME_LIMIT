package com.example.jpademo.domain.emotion.application;

import com.example.jpademo.domain.community.dao.BoardRepository;
import com.example.jpademo.domain.community.domain.Board;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class EmotionService {
    @Value("${openai.api.secret}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String url;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final BoardRepository boardRepository;

    public EmotionService(RestTemplate restTemplate, ObjectMapper objectMapper, BoardRepository boardRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.boardRepository = boardRepository;
    }

    @Transactional
    public String processEmotion(Long boardId) {
        log.info("[processConsulting] 비동기 메서드 시작, Board ID: {}", boardId);

        try {
            // 게시글 조회
            log.info("[processEmotion] 게시글 조회 중 BoardId: {}", boardId);
            Board board = boardRepository.findById(boardId)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
            log.info("[processEmotion] 게시글 조회 성공: {}", board.getContent());

            // 감정에 따라 ChatGPT 호출
            if (board.getEmotion().equals("슬픔") || board.getEmotion().equals("분노")) {
                Map<String, Object> responseMap = callChatGpt(board);
                log.info("[processEmotion] API 호출 성공, 응답 데이터: {}", responseMap);

                // ChatGPT 응답 처리
                // ChatGPT 응답 처리
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    String content = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
                    log.info("[processEmotion] 결과 데이터 생성: {}", content);

                    // EmotionContent 필드 업데이트
                    board.createEmotionContent(content);
                    boardRepository.save(board); // 변경 사항 저장

                    return content; // 반환
                } else {
                    log.error("[OpenAI API 응답 오류]");
                    throw new IllegalArgumentException("[OpenAI API 응답 오류]");
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("[processEmotion] 중 예외 발생", e);
            throw new RuntimeException("Emotion 처리 중 오류 발생", e); // 예외를 던짐
        }
    }


    public Map<String, Object> callChatGpt(Board board) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        log.info("[callChatGpt] apiKey = {}", apiKey);
        log.info("[callChatGpt] model = {}", model);

        String body = null;
        Map<String, Object> bodyMap = getEmotion(board);

        try {
            body = objectMapper.writeValueAsString(bodyMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            return objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to call OpenAI API", e);
        }
    }

    private Map<String, Object>getEmotion(Board board) {
        if (board.getEmotion().equals("슬픔")) {
            return createSorrowResponse(board, model);
        } else if (board.getEmotion().equals("분노")) {
            return createAngerResponse(board, model);
        }
        return null;
    }

    public Map<String, Object> createSorrowResponse(Board board, String model) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", model);
        log.info("'슬픔' Response");
        String systemEx =
                "Certainly! Here's the English version of your text: The service is designed for users to write emotional journals. When a user's emotion is identified as 'sadness,' the goal is to help them accept their feelings through empathy and comfort and guide them toward a positive outlook.\n"
                        + "This is the user's writing." + board.getContent() +
                        "1.1. Beginning: Empathy and Acknowledgment\n" +
                        "Read the user's journal and acknowledge their feelings of sadness with a message of empathy and understanding.\n" +
                        "Examples:\n" + "\"You're going through a tough time right now, aren't you? It's perfectly natural to feel this way.\"\n" +
                        "1.2. Middle: Advice for Emotional Comfort\n" +
                        "Provide advice to help ease the sadness and guide the user toward a more positive direction.\n" +
                        "Structure:\n" + "Convey that it's okay not to blame oneself for feeling sad.\n" + "Offer small, actionable suggestions that could bring a bit of comfort.\n" +
                        "Examples:\n" + "\"Take a step back and try deep breathing slowly. It won't erase the sadness, but it might help you feel a little better.\"\n" +
                        "1.3. Ending: Hope and Positive Encouragement\n" + "Conclude with a hopeful message, emphasizing a positive outlook for the future.\n" +
                        "Examples:\n" + "\"It’s hard right now, but remember that things will get better step by step. You're doing great. Always attach an emoji at the very end of your last sentence." +"Always answer in Korean.";

        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", board.getContent()),
                Map.of("role", "system", "content", systemEx)
        );

        bodyMap.put("messages", messages);
        return bodyMap;
    }

    public Map<String, Object> createAngerResponse(Board board, String model) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", model);
        log.info("'분노' Response");
        String systemEx =
                "This is a service where users can write emotional journals. When the user's emotion is identified as 'anger,' the goal is to provide ways to release that emotion and guide them to reflect calmly on the situation.\n" +
                        "This is the user's writing." +  board.getContent() +
                        "1.1 Beginning: Acknowledging and Empathizing with the Emotion\n" +
                        "Read the user's journal and help them recognize and accept their feelings of anger.\n" +
                        "Examples:\n" + "\"I can feel the deep anger you're experiencing right now. It's completely natural to feel this way.\"\n" +
                        "1.2. Middle: Offering Practical Ways to Manage Anger\n" +
                        "Provide concrete suggestions for effectively releasing anger.\n" +
                        "Structure:\n" + "Propose simple actions to temporarily step away from the current situation.\n" + "Introduce healthy ways to express anger.\n" +
                        "Examples:\n" + "\"First, try taking three deep breaths, inhaling and exhaling slowly.\"\n" +
                        "2.3. Ending: Transforming Anger into Positivity\n" +
                        "Conclude with a message that encourages turning anger into a positive force, preventing it from escalating into harmful actions.\n" +
                        "Examples:\n" +
                        "\"Sometimes, anger is a signal that helps us realize what truly matters to us. You might find this emotion useful in understanding your needs.Always attach an emoji at the very end of your last sentence." +
                        "Always answer in Korean.";

        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", board.getContent()),
                Map.of("role", "system", "content", systemEx)
        );

        bodyMap.put("messages", messages);
        return bodyMap;
    }
}
