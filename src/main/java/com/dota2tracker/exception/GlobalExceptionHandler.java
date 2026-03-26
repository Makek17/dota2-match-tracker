package com.dota2tracker.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Глобальний обробник помилок на основі {@code ProblemDetail} (RFC 7807).
 * Усі помилки повертаються як JSON у стандартизованому форматі.
 *
 * <p><b>Важливо:</b> внутрішні виключення Spring MVC (наприклад {@link NoResourceFoundException})
 * тут не перехоплюються - вони мають дійти до {@code DefaultHandlerExceptionResolver},
 * щоб повернути правильний HTTP-статус (404, 405 тощо), а не 500.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Порушення бізнес-правил із сервісного шару → 409 Conflict. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        detail.setTitle("Business Rule Violation");
        return detail;
    }

    /** Validation-помилки з анотацій {@code @Valid} → 400 Bad Request. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", errors);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        detail.setTitle("Validation Failed");
        return detail;
    }

    /**
     * Catch-all для решти виключень → 500 Internal Server Error.
     * Внутрішні Spring MVC-виключення перекидаються далі, щоб фреймворк
     * сам повернув правильний статус.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        // Внутрішні Spring MVC-виключення - не чіпаємо, нехай обробляє DefaultHandlerExceptionResolver
        if (ex instanceof org.springframework.web.servlet.resource.NoResourceFoundException
                || ex instanceof org.springframework.web.HttpRequestMethodNotSupportedException
                || ex instanceof org.springframework.web.HttpMediaTypeNotSupportedException) {
            throw new RuntimeException(ex);
        }

        log.error("Unhandled exception on request: {}", ex.getMessage(), ex);
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().getSimpleName() + ": " + ex.getMessage()
        );
        detail.setTitle("Internal Server Error");
        return detail;
    }
}
