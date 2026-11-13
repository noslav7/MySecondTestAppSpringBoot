package ru.rachkov.MySecondTestAppSpringBoot.service;

import ru.rachkov.MySecondTestAppSpringBoot.model.Positions;

public interface AnnualBonusService {

    double calculate(Positions positions, double salary, double bonus, int workDays);
}
