package com.twitterclone.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnauthorizedException extends RuntimeException {
    UnauthorizedException(String message) {
        super(message)
    }
}
