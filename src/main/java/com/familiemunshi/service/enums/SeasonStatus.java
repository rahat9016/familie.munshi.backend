package com.familiemunshi.service.enums;

import com.familiemunshi.service.exceptions.InvalidSeasonStatusException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Stored by enum name (ON_PLANNING); exposed in JSON by label ("On Planning").
 * Parsing accepts either form, case-insensitive.
 */
public enum SeasonStatus {
    ON_PLANNING("On Planning"),
    ON_CONCEPT("On Concept"),
    ON_PROCESS("On Process"),
    COMPLETED("Completed");

    private final String label;

    SeasonStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static SeasonStatus from(String value) {
        if (value == null) return null;
        String v = value.trim();
        return Arrays.stream(values())
                .filter(s -> s.label.equalsIgnoreCase(v) || s.name().equalsIgnoreCase(v))
                .findFirst()
                .orElseThrow(() -> new InvalidSeasonStatusException(value));
    }
}
