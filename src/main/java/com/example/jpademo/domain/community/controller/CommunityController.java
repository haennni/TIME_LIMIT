package com.example.jpademo.domain.community.controller;

import com.example.jpademo.domain.community.application.CommentService;
import com.example.jpademo.domain.community.application.BoardService;
import com.example.jpademo.domain.community.dto.BoardDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping()
public class CommunityController {

    private final CommentService commentService;
    private final BoardService boardService;

    public CommunityController(CommentService commentService, BoardService boardService) {
        this.commentService = commentService;
        this.boardService = boardService;
    }

    @RequestMapping("home")
    public String list(Model model) {
        model.addAttribute("Boards", boardService.getRecentBoards());
        return "list";
    }

    @GetMapping("home/expiring")
    public String getExpiringBoards(Model model) {
        List<BoardDTO> boards = boardService.getPostsWithin23To24Hours();
        model.addAttribute("Boards", boards);
        model.addAttribute("currentCategory", "recent-expiring");
        return "list";
    }

    @GetMapping("home/popular")
    public String getPopularBoards(Model model) {
        List<BoardDTO> boards = boardService.getPopularBoards();
        model.addAttribute("Boards", boards);
        model.addAttribute("currentCategory", "popular");
        return "list";
    }

    // 게시글 작성 폼: 로그인 필요
    @GetMapping("board/addform")
    public String addform(HttpSession session) {
        checkLogin(session);
        return "addform";
    }

    @RequestMapping("board/add")
    public String add(@ModelAttribute BoardDTO boardDto,
                      HttpSession session) throws IOException {
        Long userId = checkLogin(session); // 로그인 여부 확인 및 사용자 ID 가져오기
        boardDto.setUserId(userId);
        boardService.save(boardDto);
        return "redirect:/home";
    }


    // 게시글 읽기: 로그인하지 않아도 접근 가능
    @RequestMapping("board/{id}")
    public String read(@PathVariable long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            userId = -1L; // 비로그인 사용자를 위한 기본 값
        }
        model.addAttribute("Board", boardService.findById(id, userId));
        return "read";
    }

    // 게시글 삭제: 로그인 필요
    @RequestMapping("board/delete/{id}")
    public String delete(@PathVariable long id, HttpSession session) {
        checkLogin(session);
        boardService.deleteById(id);
        return "redirect:/home";
    }

    // 게시글 수정 폼: 로그인 필요
    @GetMapping("board/update/{id}")
    public String updateBoard(@PathVariable Long id, Model model, HttpSession session) {
        Long userId = checkLogin(session);
        BoardDTO Board = boardService.findById(id, userId);
        model.addAttribute("Board", Board);
        return "updateform";
    }

    // 게시글 수정: 로그인 필요
    @PostMapping("board/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute BoardDTO boardDto, HttpSession session) {
        Long userId = checkLogin(session);
        boardDto.setUserId(userId); // 사용자 ID 설정
        boardService.update(id, boardDto); // 업데이트 호출
        return "redirect:/board/" + id;
    }

    // 게시글 검색: 로그인하지 않아도 접근 가능
    @RequestMapping("board/search")
    public String search(Model model, @RequestParam String search) {
        List<BoardDTO> findByTitleBoard = boardService.findByTitle(search);
        model.addAttribute("findTitle", findByTitleBoard);
        return "list";
    }

    // 댓글 작성: 로그인 필요
    @PostMapping("board/{boardId}/comments")
    public String addComment(@PathVariable Long boardId, @RequestParam String content, HttpSession session) {
        Long userId = checkLogin(session); // 로그인 여부 확인 및 사용자 ID 가져오기
        commentService.addComment(boardId, userId, content);
        return "redirect:/board/" + boardId; // 현재 게시글로 리다이렉트
    }

    // 감정별 게시글 조회
    @RequestMapping("board/searchEmotion")
    public String searchEmotion(Model model, @RequestParam String search) {
        List<BoardDTO> findByEmotion = boardService.findByEmotion(search);
        model.addAttribute("findEmotion", findByEmotion);
        return "list";
    }

    // 로그인 여부를 확인하는 메서드
    private Long checkLogin(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return userId;
    }
}
