package ru.rachkov.MySecondTestAppSpringBoot.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import ru.rachkov.MySecondTestAppSpringBoot.exception.ValidationFailedException;

import java.util.stream.Collectors;

@Service
public class RequestValidationService implements ValidationService {
    @Override
    public void isValid(BindingResult bindingResult) throws ValidationFailedException {
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new ValidationFailedException("Ошибка валидации: " + errors);
        }
    }
}
