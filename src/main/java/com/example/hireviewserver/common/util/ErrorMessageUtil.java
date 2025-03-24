package com.example.hireviewserver.common.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorMessageUtil {
    public static String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    public static String formatFieldErrors(List<FieldError> errors) {
        return errors.stream()
                .map(ErrorMessageUtil::formatFieldError)
                .collect(Collectors.joining("; "));
    }
}
