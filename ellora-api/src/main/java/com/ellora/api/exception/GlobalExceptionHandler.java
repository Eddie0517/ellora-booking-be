package com.ellora.api.exception;

import com.ellora.domain.exception.BusinessRuleException;
import com.ellora.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler transforming exceptions into RFC 9457 Problem Details.
 * Standardizes non-2xx responses without proprietary response wrappers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record InvalidParam(String name, String reason) {}

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setType(URI.create("https://ellora.vn/errors/not-found"));
        problemDetail.setTitle("Tài nguyên không tồn tại");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("traceId", UUID.randomUUID().toString());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Tham số đầu vào không hợp lệ hoặc thiếu dữ liệu bắt buộc.");
        problemDetail.setType(URI.create("https://ellora.vn/errors/validation-error"));
        problemDetail.setTitle("Dữ liệu không hợp lệ");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("traceId", UUID.randomUUID().toString());

        List<InvalidParam> invalidParams = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            invalidParams.add(new InvalidParam(fieldError.getField(), fieldError.getDefaultMessage()));
        }
        problemDetail.setProperty("invalidParams", invalidParams);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRuleException(
            BusinessRuleException ex, HttpServletRequest request) {
        log.warn("Business rule violation at {}: {}", request.getRequestURI(), ex.getMessage());
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create("https://ellora.vn/errors/business-rule-violation"));
        problemDetail.setTitle("Vi phạm quy tắc nghiệp vụ");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("traceId", UUID.randomUUID().toString());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(DomainException ex, HttpServletRequest request) {
        log.warn("Domain exception at {}: {}", request.getRequestURI(), ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://ellora.vn/errors/domain-error"));
        problemDetail.setTitle("Lỗi nghiệp vụ cơ bản");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("traceId", UUID.randomUUID().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error(
                "Unhandled server error [traceId={}] at {}: {}", traceId, request.getRequestURI(), ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        problemDetail.setType(URI.create("https://ellora.vn/errors/internal-server-error"));
        problemDetail.setTitle("Lỗi hệ thống nội bộ");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("traceId", traceId);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }
}
