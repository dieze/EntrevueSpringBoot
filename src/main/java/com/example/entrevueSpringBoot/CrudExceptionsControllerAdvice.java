package com.example.entrevueSpringBoot;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.Map;

@RestControllerAdvice
public class CrudExceptionsControllerAdvice {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handle(EntityNotFoundException e) {
        return Collections.singletonMap(
                String.format("%s not found", e.getEntityName()),
                e.getFindByCriteria()
        );
    }
}
