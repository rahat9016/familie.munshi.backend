package com.familiemunshi.http.dtos.responses;

import lombok.*;

@Getter
@Setter
public class RefreshTokenResponse {
    private String accessToken;

    public RefreshTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
