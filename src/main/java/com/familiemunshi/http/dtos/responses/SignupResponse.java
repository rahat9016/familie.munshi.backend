package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.User;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
public class SignupResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isActive;

    public SignupResponse(User user){
        this.id = user.id();
        this.firstName = user.firstName();
        this.lastName = user.lastName();
        this.email = user.email();
        this.isActive = user.isActive();
    }
}
