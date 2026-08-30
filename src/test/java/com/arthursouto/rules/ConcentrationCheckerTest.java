package com.arthursouto.rules;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ConcentrationCheckerTest {

    @Test
    void returnsNoDataWhenNoRangeIsDefined() {
        assertThat(ConcentrationChecker.classify(null, null, BigDecimal.TEN))
                .isEqualTo(ConcentrationStatus.NO_DATA);
    }

    @Test
    void returnsBelowMinWhenValueIsUnderMinimum() {
        assertThat(ConcentrationChecker.classify(BigDecimal.TEN, BigDecimal.valueOf(20), BigDecimal.valueOf(5)))
                .isEqualTo(ConcentrationStatus.BELOW_MIN);
    }

    @Test
    void returnsAboveMaxWhenValueExceedsMaximum() {
        assertThat(ConcentrationChecker.classify(BigDecimal.TEN, BigDecimal.valueOf(20), BigDecimal.valueOf(25)))
                .isEqualTo(ConcentrationStatus.ABOVE_MAX);
    }

    @Test
    void returnsWithinRangeWhenValueIsBetweenMinAndMax() {
        assertThat(ConcentrationChecker.classify(BigDecimal.TEN, BigDecimal.valueOf(20), BigDecimal.valueOf(15)))
                .isEqualTo(ConcentrationStatus.WITHIN_RANGE);
    }
}
