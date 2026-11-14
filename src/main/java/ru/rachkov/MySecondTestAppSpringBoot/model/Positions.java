package ru.rachkov.MySecondTestAppSpringBoot.model;

import lombok.Getter;

@Getter
public enum Positions {

    DEV(2.2, false), // Разработчик
    HR(1.2, false),  // Специалист по персоналу
    TL(2.6, true),   // Руководитель команды
    PO(2.8, true),   // Владелец продукта
    TPM(3.0, true),  // Технический продакт-менеджер
    CTO(3.5, true);  // Технический директор

    private final double positionCoefficient;
    private final boolean isManager;

    Positions(double positionCoefficient, boolean isManager) {
        this.positionCoefficient = positionCoefficient;
        this.isManager = isManager;
    }
}
