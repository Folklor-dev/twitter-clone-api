package com.twitterclone.exceptions

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class)

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.error("Resource not found: {}", ex.getMessage())
        return new ResponseEntity<>(
            new ErrorResponse(
                status: HttpStatus.NOT_FOUND.value(),
                message: ex.getMessage(),
                timestamp: System.currentTimeMillis()
            ),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler([ForbiddenException.class, AccessDeniedException.class])
    ResponseEntity<ErrorResponse> handleForbiddenException(Exception ex) {
        logger.error("Forbidden: {}", ex.getMessage())
        return new ResponseEntity<>(
            new ErrorResponse(
                status: HttpStatus.FORBIDDEN.value(),
                message: ex.getMessage(),
                timestamp: System.currentTimeMillis()
            ),
            HttpStatus.FORBIDDEN
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        def errors = [:]
        ex.getBindingResult().getAllErrors().each {
            String fieldName = ((FieldError) it).getField()
            String errorMessage = it.getDefaultMessage()
            errors[fieldName] = errorMessage
        }

        logger.error("Validation error: {}", errors)

        return new ResponseEntity<>(
            new ValidationErrorResponse(
                status: HttpStatus.BAD_REQUEST.value(),
                message: "Validation failed",
                errors: errors,
                timestamp: System.currentTimeMillis()
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler([UnauthorizedException.class, BadCredentialsException.class])
    ResponseEntity<ErrorResponse> handleUnauthorizedException(Exception ex) {
        logger.error("Authorization error: {}", ex.getMessage())
        return new ResponseEntity<>(
            new ErrorResponse(
                status: HttpStatus.UNAUTHORIZED.value(),
                message: ex.getMessage(),
                timestamp: System.currentTimeMillis()
            ),
            HttpStatus.UNAUTHORIZED
        )
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        logger.error("Bad request: {}", ex.getMessage())
        return new ResponseEntity<>(
            new ErrorResponse(
                status: HttpStatus.BAD_REQUEST.value(),
                message: ex.getMessage(),
                timestamp: System.currentTimeMillis()
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        logger.error("Unhandled exception", ex)
        return new ResponseEntity<>(
            new ErrorResponse(
                status: HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message: "Internal server error: " + ex.getMessage(),
                timestamp: System.currentTimeMillis()
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}

class ErrorResponse {
    int status
    String message
    long timestamp
}

class ValidationErrorResponse extends ErrorResponse {
    Map<String, String> errors
}
