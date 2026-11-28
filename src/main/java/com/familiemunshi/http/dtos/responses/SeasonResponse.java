package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.Season;
import com.familiemunshi.service.enums.SeasonStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class SeasonResponse {

    private final UUID id;
    private final String season;
    private final long numberOfStyles;
    private final SeasonStatus status; // serialized as label via @JsonValue
    private final Instant createdAt;
    private final Instant updatedAt;

    public SeasonResponse(Season s, long numberOfStyles) {
        this.id = s.id();
        this.season = s.season();
        this.numberOfStyles = numberOfStyles;
        this.status = s.status();
        this.createdAt = s.createdAt();
        this.updatedAt = s.updatedAt();
    }
}
