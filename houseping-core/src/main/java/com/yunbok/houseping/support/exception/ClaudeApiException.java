package com.yunbok.houseping.support.exception;

public class ClaudeApiException extends ExternalApiException {

    public ClaudeApiException(String message) {
        super("[Claude API] " + message);
    }

    public ClaudeApiException(Throwable cause) {
        super("Claude", cause);
    }
}
