package com.example.board.global;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice // 모든 컨트롤러 감시
public class GlobalExceptionHandler {
    
    /**
     * 비즈니스 로직 에러 처리(Service에서 throw한 에러)
     * 예: 중복된 아이디, 비밀번호 불일치 등
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    /**
     * @Valid 유효성 검사 실패 처리
     * 예: 아이디 빈칸, 비밀번호 길이 부족 등
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        Map<String, String> errors = new HashMap<>();

        // 에러가 난 필드의 메시지를 Map에 담기
        e.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }
}
