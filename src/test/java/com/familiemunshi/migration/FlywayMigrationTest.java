package com.familiemunshi.migration;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.metamodel.mapping.SelectableMapping;
import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Runs every Flyway migration against an empty PostgreSQL container and
 * checks that the resulting schema contains every table and column the JPA
 * entities need.
 *
 * Hibernate validation is turned off here (ddl-auto=none) on purpose: instead
 * of failing at context startup with a long stack trace, the schema test below
 * lists exactly which entity / table / column has no migration and how to fix it.
 * Column type mismatches are still caught at app startup (ddl-auto=validate).
 */
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true",
        "spring.flyway.baseline-on-migrate=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class FlywayMigrationTest {

    private static final String MIGRATION_DIR = "src/main/resources/db/migration";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void allMigrationsApplySuccessfully() {
        MigrationInfo[] applied = flyway.info().applied();

        assertThat(applied)
                .as("No Flyway migration was applied. Is %s empty?", MIGRATION_DIR)
                .isNotEmpty();

        for (MigrationInfo info : applied) {
            assertThat(info.getState())
                    .as("Migration %s (%s) did not succeed", info.getVersion(), info.getScript())
                    .isEqualTo(MigrationState.SUCCESS);
        }

        assertThat(flyway.info().pending())
                .as("Some migrations are still pending after migrate()")
                .isEmpty();
    }

    @Test
    void everyEntityHasTableAndColumnsInMigrations() {
        Map<String, Set<String>> dbColumns = loadDatabaseColumns();
        Map<String, Map<String, Set<String>>> expected = loadEntityRequirements();

        List<String> problems = new ArrayList<>();

        expected.forEach((owner, tables) -> tables.forEach((table, columns) -> {
            Set<String> existing = dbColumns.get(table);

            if (existing == null) {
                problems.add(String.format(
                        "  ✗ %-28s → table \"%s\" is missing", owner, table));
                return;
            }

            Set<String> missing = new TreeSet<>(columns);
            missing.removeAll(existing);

            for (String column : missing) {
                problems.add(String.format(
                        "  ✗ %-28s → column \"%s.%s\" is missing", owner, table, column));
            }
        }));

        if (!problems.isEmpty()) {
            fail(buildFixMessage(problems));
        }
    }

    @Test
    void reRunningMigrationsIsNoOp() {
        assertThat(flyway.migrate().migrationsExecuted)
                .as("Running migrate() a second time executed migrations again")
                .isZero();
    }

    // -----------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------

    /** table -> columns that actually exist after running all migrations. */
    private Map<String, Set<String>> loadDatabaseColumns() {
        Map<String, Set<String>> result = new HashMap<>();

        jdbcTemplate.query(
                """
                SELECT table_name, column_name
                FROM information_schema.columns
                WHERE table_schema = 'public'
                """,
                rs -> {
                    result.computeIfAbsent(rs.getString(1), t -> new HashSet<>())
                            .add(rs.getString(2));
                }
        );

        return result;
    }

    /** owner (entity / join table) -> table -> columns the JPA mapping needs. */
    private Map<String, Map<String, Set<String>>> loadEntityRequirements() {
        MappingMetamodel metamodel = entityManagerFactory
                .unwrap(SessionFactoryImplementor.class)
                .getMappingMetamodel();

        Map<String, Map<String, Set<String>>> result = new LinkedHashMap<>();

        metamodel.forEachEntityDescriptor(persister -> {
            String owner = simpleName(persister.getEntityName());
            Map<String, Set<String>> tables =
                    result.computeIfAbsent(owner, o -> new LinkedHashMap<>());

            tables.computeIfAbsent(normalize(persister.getTableName()), t -> new TreeSet<>());

            persister.getIdentifierMapping()
                    .forEachSelectable((i, s) -> addColumn(tables, s));
            persister.forEachSelectable((i, s) -> addColumn(tables, s));
        });

        metamodel.forEachCollectionDescriptor(collection -> {
            if (collection.isOneToMany()
                    || !(collection instanceof AbstractCollectionPersister persister)) {
                return;
            }

            String table = normalize(persister.getTableName());
            String owner = simpleName(persister.getRole());
            Set<String> columns = result
                    .computeIfAbsent(owner, o -> new LinkedHashMap<>())
                    .computeIfAbsent(table, t -> new TreeSet<>());

            Arrays.stream(persister.getKeyColumnNames()).map(this::normalize).forEach(columns::add);
            Arrays.stream(persister.getElementColumnNames()).map(this::normalize).forEach(columns::add);
        });

        return result;
    }

    private void addColumn(Map<String, Set<String>> tables, SelectableMapping selectable) {
        if (selectable.isFormula()) {
            return;
        }

        tables.computeIfAbsent(normalize(selectable.getContainingTableExpression()), t -> new TreeSet<>())
                .add(normalize(selectable.getSelectionExpression()));
    }

    private String buildFixMessage(List<String> problems) {
        String nextVersion = "V" + nextMigrationVersion();

        return String.join("\n",
                "",
                "",
                "══════════════════════════════════════════════════════════════════",
                " DATABASE MIGRATION MISSING",
                " JPA entities need tables/columns that no Flyway migration creates.",
                "══════════════════════════════════════════════════════════════════",
                "",
                String.join("\n", problems),
                "",
                " How to fix:",
                "  1. Create a NEW migration file:",
                "       " + MIGRATION_DIR + "/" + nextVersion + "__<description>.sql",
                "  2. Add CREATE TABLE / ALTER TABLE ... ADD COLUMN for the items above.",
                "  3. Do NOT edit migrations that were already applied (checksum mismatch).",
                "  4. Re-run: ./mvnw clean test -Dtest=FlywayMigrationTest",
                "",
                " Without this migration the app will FAIL TO START on the server",
                " (Hibernate ddl-auto=validate → \"Schema validation: missing ...\").",
                "══════════════════════════════════════════════════════════════════",
                "");
    }

    private int nextMigrationVersion() {
        return Arrays.stream(flyway.info().applied())
                .filter(info -> info.getVersion() != null)
                .mapToInt(info -> info.getVersion().getMajor().intValue())
                .max()
                .orElse(0) + 1;
    }

    private String simpleName(String name) {
        int dot = name.lastIndexOf('.', name.lastIndexOf('.') - 1);
        String trimmed = name;

        // Entity: com.x.UserDao -> UserDao ; collection role: com.x.RoleDao.permissions -> RoleDao.permissions
        if (Character.isUpperCase(name.charAt(name.lastIndexOf('.') + 1))) {
            trimmed = name.substring(name.lastIndexOf('.') + 1);
        } else if (dot >= 0) {
            trimmed = name.substring(dot + 1);
        }

        return trimmed;
    }

    private String normalize(String identifier) {
        String value = identifier.replace("\"", "");
        int dot = value.lastIndexOf('.');

        return (dot >= 0 ? value.substring(dot + 1) : value).toLowerCase(Locale.ROOT);
    }
}
