package ru.rachkov.MySecondTestAppSpringBoot.service;

import org.springframework.stereotype.Service;
import ru.rachkov.MySecondTestAppSpringBoot.model.Positions;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class AnnualBonusServiceImpl implements AnnualBonusService {

    private final Clock clock;

    public AnnualBonusServiceImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public double calculate(Positions positions, double salary, double bonus, int workDays) {
        int daysInYear = LocalDate.now(clock).lengthOfYear();
        return calculateForPeriod(positions, salary, bonus, workDays, daysInYear);
    }

    @Override
    public double calculateQuarterly(Positions positions, double salary, double bonus, int workDays) {
        if (positions == null || !positions.isManager()) {
            throw new IllegalArgumentException("Квартальная премия доступна только управленцам");
        }

        LocalDate today = LocalDate.now(clock);
        int firstMonthOfQuarter = (today.getMonthValue() - 1) / 3 * 3 + 1;
        LocalDate quarterStart = LocalDate.of(today.getYear(), firstMonthOfQuarter, 1);
        int daysInQuarter = (int) ChronoUnit.DAYS.between(quarterStart, quarterStart.plusMonths(3));
        return calculateForPeriod(positions, salary, bonus, workDays, daysInQuarter);
    }

    private double calculateForPeriod(Positions positions, double salary, double bonus,
                                      int workDays, int periodDays) {
        if (workDays <= 0) {
            throw new IllegalArgumentException("Количество рабочих дней должно быть больше нуля");
        }

        return salary * bonus * periodDays * positions.getPositionCoefficient() / workDays;
    }
}
