package net.likelion.bebc25.fixgo.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String name();

    HttpStatus getStatus();
}
