package com.arthursouto.rules;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class ConcentrationChecker {

    public static ConcentrationStatus classify(BigDecimal min, BigDecimal max, BigDecimal value) {
        if (min == null && max == null) {
            return ConcentrationStatus.NO_DATA;
        }
        if (min != null && value.compareTo(min) < 0) {
            return ConcentrationStatus.BELOW_MIN;
        }
        if (max != null && value.compareTo(max) > 0) {
            return ConcentrationStatus.ABOVE_MAX;
        }
        return ConcentrationStatus.WITHIN_RANGE;
    }
}
