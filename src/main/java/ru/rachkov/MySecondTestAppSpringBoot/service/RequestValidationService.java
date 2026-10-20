package ru.rachkov.MySecondTestAppSpringBoot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import ru.rachkov.MySecondTestAppSpringBoot.exception.ValidationFailedException;

import java.util.stream.Collectors;

@Slf4j
@Service
public class RequestValidationService implements ValidationService {
    @Override
    public void isValid(BindingResult bindingResult) throws ValidationFailedException {
        log.info("Validating request: {}", bindingResult.getTarget());
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> (error instanceof FieldError fieldError
                            ? fieldError.getField() : error.getObjectName())
                            + ": " + error.getDefaultMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            log.error("Request validation failed, request={}: {}", bindingResult.getTarget(), errors);
            throw new ValidationFailedException("Ошибка валидации: " + errors);
        }
        log.info("Request validation passed: {}", bindingResult.getTarget());
    }
}
