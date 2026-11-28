package com.familiemunshi.common.configs;

import com.familiemunshi.service.enums.SeasonStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/** Binds query params like ?status=On Process (Jackson's @JsonCreator does not apply there). */
@Component
public class SeasonStatusConverter implements Converter<String, SeasonStatus> {
    @Override
    public SeasonStatus convert(String source) {
        return source.isBlank() ? null : SeasonStatus.from(source);
    }
}
