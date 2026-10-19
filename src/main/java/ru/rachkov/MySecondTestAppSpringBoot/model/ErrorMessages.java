package ru.rachkov.MySecondTestAppSpringBoot.model;

public enum ErrorMessages {

    EMPTY(""),

    VALIDATION("Ошибка валидации"),

    UNSUPORTED("Произошла непредвиденная ошибка"),

    UNKNOWN("Неподдерживаемая ошибка");

    private String description;

    ErrorMessages(String description) {
        this.description = description;
    }

    public String getName() {
        return description;
    }
}
