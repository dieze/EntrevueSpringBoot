package com.example.entrevueSpringBoot;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.*;

@ControllerAdvice
@RequiredArgsConstructor
public class ValidationExceptionsControllerAdvice {

    private final ValidatorFactory validatorFactory;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, List<String>> handle(MethodArgumentNotValidException exception) {
        Map<String, List<String>> propertyToViolations = new LinkedHashMap<>(); // LinkedHashMap preserves order

        for (ObjectError objectError : exception.getAllErrors()) {
            ConstraintViolation<?> constraintViolation = objectError.unwrap(ConstraintViolation.class);
            String property = constraintViolation.getPropertyPath().toString();
            if (!propertyToViolations.containsKey(property)) {
                propertyToViolations.put(property, new ArrayList<>());
            }
            String englishMessage = englishMessage(constraintViolation);
            propertyToViolations.get(property).add(englishMessage);
        }

        return propertyToViolations;
    }

    private String englishMessage(ConstraintViolation<?> constraintViolation) {
        return validatorFactory.getMessageInterpolator().interpolate(
                constraintViolation.getMessageTemplate(),
                new MessageInterpolator.Context() {
                    @Override
                    public ConstraintDescriptor<?> getConstraintDescriptor() {
                        return constraintViolation.getConstraintDescriptor();
                    }

                    @Override
                    public Object getValidatedValue() {
                        return constraintViolation.getInvalidValue();
                    }

                    @Override
                    public <T> T unwrap(Class<T> type) {
                        return constraintViolation.unwrap(type);
                    }
                },
                Locale.US
        );
    }
}
