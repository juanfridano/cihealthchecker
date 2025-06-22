package com.juanfridano.cihealthchecker.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.juanfridano.cihealthchecker.exception.GitHubClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(GitHubClientException.class)
    public ResponseEntity<Map<String, Object>> handleGitHubException(GitHubClientException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "error", "GitHub API Error",
                        "message", ex.getMessage(),
                        "status", 502
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Internal Server Error",
                        "message", ex.getMessage(),
                        "status", 500
                ));
    }
}