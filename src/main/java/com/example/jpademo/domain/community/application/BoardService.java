package com.example.jpademo.domain.community.application;

import com.example.jpademo.domain.community.dto.BoardDTO;

import java.util.List;

public interface BoardService {
    List<BoardDTO> findAll();

    BoardDTO findById(Long boardId, Long currentUserId);
    void save(BoardDTO Board);
    void deleteById(long idx);
    void update(Long idx, BoardDTO boardDto);

    List<BoardDTO> getRecentBoards();
    List<BoardDTO> findByTitle(String search);
    List<BoardDTO> findByEmotion(String search);
}
