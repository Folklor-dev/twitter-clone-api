package com.twitterclone.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class ForbiddenException extends RuntimeException {
    ForbiddenException(String message) {
        super(message)
    }
}