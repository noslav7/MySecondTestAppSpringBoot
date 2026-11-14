package ru.rachkov.MySecondTestAppSpringBoot.service;

import org.junit.jupiter.api.Test;
import ru.rachkov.MySecondTestAppSpringBoot.model.Positions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AnnualBonusServiceImplTest {

    @Test
    void calculate() {
        // given
        Positions position = Positions.HR;
        double bonus = 2.0;
        int workDays = 243;
        double salary = 100000.00;

        // when
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        double result = new AnnualBonusServiceImpl(clock).calculate(position, salary, bonus, workDays);

        // then
        double expected = 360493.8271604938;
        assertThat(result).isEqualTo(expected);
    }
}
