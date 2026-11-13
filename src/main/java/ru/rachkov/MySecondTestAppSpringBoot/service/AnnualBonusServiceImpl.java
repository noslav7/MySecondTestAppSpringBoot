package ru.rachkov.MySecondTestAppSpringBoot.service;

import org.springframework.stereotype.Service;
import ru.rachkov.MySecondTestAppSpringBoot.model.Positions;

@Service
public class AnnualBonusServiceImpl implements AnnualBonusService {

    @Override
    public double calculate(Positions positions, double salary, double bonus, int workDays) {
        if (workDays <= 0) {
            throw new IllegalArgumentException("Количество рабочих дней должно быть больше нуля");
        }

        // Формула из задания: оклад × множитель премии × 365 × коэффициент должности / рабочие дни.
        return salary * bonus * 365 * positions.getPositionCoefficient() / workDays;
    }
}
