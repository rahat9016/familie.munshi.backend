package com.familiemunshi.common.utils;


import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;

import java.util.UUID;


public class UuidV7Generator {
    private static final TimeBasedEpochRandomGenerator GENERATOR =
            Generators.timeBasedEpochRandomGenerator();

    private UuidV7Generator() {
    }

    public static UUID generate() {
        return GENERATOR.generate();
    }
}
