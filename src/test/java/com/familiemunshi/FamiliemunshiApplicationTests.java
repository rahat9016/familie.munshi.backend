package com.familiemunshi;

import com.familiemunshi.common.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Smoke test: starts the FULL application context (security, services,
 * Flyway, Hibernate ddl-auto=validate, seeders) against a throw-away
 * PostgreSQL container. Fails if any bean cannot be created or the schema
 * does not match the entities (including column types).
 *
 * File storage (MinIO/S3) is an external service and is mocked:
 * storage.type=none disables both real storage implementations.
 */
@SpringBootTest(properties = {
        "jwt.secret=dGVzdC1vbmx5LXNlY3JldC1rZXktMzItYnl0ZXMtbG9uZyEh",
        "branch-admin.email-domain=example.test",
        "branch-admin.common-password=test-password-123",
        "storage.type=none",
        "spring.flyway.baseline-on-migrate=false"
})
@Testcontainers
class FamiliemunshiApplicationTests {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:16-alpine");

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    void contextLoads() {
    }
}
