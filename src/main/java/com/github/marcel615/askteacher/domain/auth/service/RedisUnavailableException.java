package com.github.marcel615.askteacher.domain.auth.service;

public class RedisUnavailableException extends RuntimeException {
    public RedisUnavailableException(Throwable cause) {
        super(cause);
    }
}
