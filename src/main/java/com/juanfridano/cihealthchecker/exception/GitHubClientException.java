package com.juanfridano.cihealthchecker.exception;

public class GitHubClientException extends RuntimeException {
    public GitHubClientException(String message) {
        super(message);
    }
}