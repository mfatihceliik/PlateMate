package com.mefy.platemate.core.exceptions;

import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Tüm controller'lardaki exception'ları merkezi olarak yakalar.
 * Her bir hata tipi için uygun HTTP status code ve mesaj döner.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Validation hataları (@Valid annotation'dan gelen)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDataResult<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        return new ResponseEntity<>(
                new ErrorDataResult<>(errors, "Doğrulama hataları"),
                HttpStatus.BAD_REQUEST
        );
    }

    // Genel exception'lar
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDataResult<String>> handleGeneralExceptions(Exception ex) {
        return new ResponseEntity<>(
                new ErrorDataResult<>(ex.getMessage(), "Beklenmeyen bir hata oluştu."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
