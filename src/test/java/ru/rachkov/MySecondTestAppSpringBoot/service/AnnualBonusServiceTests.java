package ru.rachkov.MySecondTestAppSpringBoot.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.rachkov.MySecondTestAppSpringBoot.model.Positions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class AnnualBonusServiceTests {

    private final AnnualBonusService service = new AnnualBonusServiceImpl(
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

    @ParameterizedTest
    @CsvSource({
            "DEV, 100000, 1.5, 365, 330000",
            "HR,  100000, 1.5, 365, 180000",
            "TL,  100000, 1.5, 365, 390000",
            "DEV, 100000, 1.5, 250, 481800",
            "DEV,      0, 1.5, 250,      0",
            "DEV, 100000,   0, 250,      0"
    })
    void calculatesAnnualBonus(Positions position, double salary, double bonus,
                              int workDays, double expected) {
        assertThat(service.calculate(position, salary, bonus, workDays))
                .isCloseTo(expected, within(0.000001));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -250})
    void rejectsNonPositiveWorkDays(int workDays) {
        assertThatThrownBy(() -> service.calculate(Positions.DEV, 100000, 1.5, workDays))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "2026-01-01T00:00:00Z, 365000",
            "2024-02-29T00:00:00Z, 366000",
            "1900-03-01T00:00:00Z, 365000",
            "2000-03-01T00:00:00Z, 366000",
            "2100-03-01T00:00:00Z, 365000"
    })
    void usesActualLengthOfYear(String instant, double expected) {
        AnnualBonusService datedService = new AnnualBonusServiceImpl(
                Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));

        assertThat(datedService.calculate(Positions.HR, 100000, 2, 240))
                .isCloseTo(expected, within(0.000001));
    }
}
