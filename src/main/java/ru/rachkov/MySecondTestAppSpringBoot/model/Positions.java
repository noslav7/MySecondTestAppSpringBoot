package ru.rachkov.MySecondTestAppSpringBoot.model;

import lombok.Getter;

@Getter
public enum Positions {

    DEV(2.2), // Developer
    HR(1.2),  // HR-specialist
    TL(2.6);  // Manager

    private final double positionCoefficient;

    Positions(double positionCoefficient) {
        this.positionCoefficient = positionCoefficient;
    }
}
