package com.mefy.platemate.core.exceptions;

import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final IMessageService messageService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDataResult<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        return new ResponseEntity<>(
                new ErrorDataResult<>(errors, messageService.getMessage(Messages.VALIDATION_ERROR)),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<ErrorDataResult<String>> handleInvalidPaginationException(InvalidPaginationException ex) {
        return new ResponseEntity<>(
                new ErrorDataResult<>(
                        messageService.getMessage(ex.getMessageKey(), ex.getArgs()),
                        messageService.getMessage(Messages.PAGINATION_INVALID)
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDataResult<String>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch exception", ex);
        return new ResponseEntity<>(
                new ErrorDataResult<>(messageService.getMessage(Messages.INVALID_REQUEST_PARAM)),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDataResult<String>> handleGeneralExceptions(Exception ex) {
        log.error("Unhandled exception", ex);
        return new ResponseEntity<>(
                new ErrorDataResult<>(messageService.getMessage(Messages.UNEXPECTED_ERROR)),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
