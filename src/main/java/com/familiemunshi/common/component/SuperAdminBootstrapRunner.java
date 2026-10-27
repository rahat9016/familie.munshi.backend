package com.familiemunshi.common.component;


import com.familiemunshi.service.SuperAdminBootstrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class SuperAdminBootstrapRunner implements ApplicationRunner {
    private final SuperAdminBootstrapService bootstrapService;

    @Value("${bootstrap.super-admin.enabled:false}")
    private boolean enabled;

    @Value("${bootstrap.super-admin.first-name:}")
    private String firstName;

    @Value("${bootstrap.super-admin.last-name:}")
    private String lastName;

    @Value("${bootstrap.super-admin.email:}")
    private String email;

    @Value("${bootstrap.super-admin.password:}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {

        if (!enabled) {
            return;
        }

        bootstrapService.initialize(
                firstName,
                lastName,
                email,
                password
        );
    }
}
