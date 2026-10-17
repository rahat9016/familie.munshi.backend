package com.familiemunshi.http.dtos.responses;

import com.familiemunshi.service.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isActive;

    public UserResponse(User user){
        this.id = user.id();
        this.firstName = user.firstName();
        this.lastName = user.lastName();
        this.email = user.email();
        this.isActive = user.isActive();
    }
}
