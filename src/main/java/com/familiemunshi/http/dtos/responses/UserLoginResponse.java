package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserLoginResponse {
    private UUID id;
    private String accessToken;
    private String refreshToken;

    public UserLoginResponse(UUID id, String accessToken, String refreshToken) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
