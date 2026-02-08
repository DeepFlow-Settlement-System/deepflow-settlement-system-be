package com.deepflow.settlementsystem.common;

import org.springframework.stereotype.Component;

@Component
public class NumberParser {

    public Integer parseInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public int parseIntOrZero(String value) {
        Integer parsed = parseInt(value);
        return parsed == null ? 0 : parsed;
    }
}
