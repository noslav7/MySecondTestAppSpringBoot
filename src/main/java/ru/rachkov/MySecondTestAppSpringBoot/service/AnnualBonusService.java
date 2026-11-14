package ru.rachkov.MySecondTestAppSpringBoot.service;

import ru.rachkov.MySecondTestAppSpringBoot.model.Positions;

public interface AnnualBonusService {

    /** Рассчитывает годовую премию за текущий год; workDays — отработанные дни за год. */
    double calculate(Positions positions, double salary, double bonus, int workDays);

    /**
     * Рассчитывает премию управленца: оклад × множитель премии × дни текущего квартала
     * × коэффициент должности / отработанные дни за квартал.
     *
     * @throws IllegalArgumentException если должность не управленческая или workDays <= 0
     */
    double calculateQuarterly(Positions positions, double salary, double bonus, int workDays);
}
