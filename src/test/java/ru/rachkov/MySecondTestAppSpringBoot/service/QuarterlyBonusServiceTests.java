package ru.rachkov.MySecondTestAppSpringBoot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.rachkov.MySecondTestAppSpringBoot.model.Positions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class QuarterlyBonusServiceTests {

    private final AnnualBonusService service = serviceAt("2026-01-01T00:00:00Z");

    @ParameterizedTest
    @CsvSource({
            "TL,  100000, 1.5, 60, 585000",
            "PO,  100000, 1.5, 60, 630000",
            "TPM, 100000, 1.5, 60, 675000",
            "CTO, 100000, 1.5, 60, 787500",
            "TL,       0, 1.5, 60,      0",
            "TL,  100000,   0, 60,      0"
    })
    void calculatesQuarterlyBonusForManagers(Positions position, double salary, double bonus,
                                            int workDays, double expected) {
        assertThat(service.calculateQuarterly(position, salary, bonus, workDays))
                .isCloseTo(expected, within(0.000001));
    }

    @ParameterizedTest
    @CsvSource({
            "2026-01-01T00:00:00Z, 270000",
            "2026-03-31T23:59:59Z, 270000",
            "2026-04-01T00:00:00Z, 273000",
            "2026-07-01T00:00:00Z, 276000",
            "2026-10-01T00:00:00Z, 276000",
            "2026-12-31T23:59:59Z, 276000",
            "2027-01-01T00:00:00Z, 270000",
            "2024-02-29T00:00:00Z, 273000",
            "2000-01-01T00:00:00Z, 273000",
            "2100-01-01T00:00:00Z, 270000"
    })
    void usesActualLengthOfQuarter(String instant, double expected) {
        assertThat(serviceAt(instant).calculateQuarterly(Positions.TPM, 100000, 1, 100))
                .isCloseTo(expected, within(0.000001));
    }

    @ParameterizedTest
    @EnumSource(value = Positions.class, names = {"DEV", "HR"})
    void rejectsNonManagers(Positions position) {
        assertThat(position.isManager()).isFalse();
        assertThatThrownBy(() -> service.calculateQuarterly(position, 100000, 1.5, 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Квартальная премия доступна только управленцам");
    }

    @Test
    void rejectsMissingPosition() {
        assertThatThrownBy(() -> service.calculateQuarterly(null, 100000, 1.5, 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Квартальная премия доступна только управленцам");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -60})
    void rejectsNonPositiveWorkDays(int workDays) {
        assertThatThrownBy(() -> service.calculateQuarterly(Positions.TL, 100000, 1.5, workDays))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Количество рабочих дней должно быть больше нуля");
    }

    private static AnnualBonusService serviceAt(String instant) {
        return new AnnualBonusServiceImpl(Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }
}
