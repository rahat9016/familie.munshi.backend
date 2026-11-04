package com.familiemunshi.common.component;

import com.familiemunshi.jpa.daos.PermissionDao;
import com.familiemunshi.jpa.repositories.PermissionRepository;
import com.familiemunshi.service.enums.PermissionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class PermissionSeeder implements ApplicationRunner {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        int inserted = 0;
        int updated = 0;
        int unchanged = 0;

        for (PermissionCode permission : PermissionCode.values()) {

            // 1. FIND EXISTING PERMISSION
            PermissionDao dao = permissionRepository
                    .findByName(permission.getCode())
                    .orElse(null);

            // 2. CREATE IF NOT EXISTS
            if (dao == null) {

                PermissionDao newPermission = PermissionDao.builder()
                        .name(permission.getCode())
                        .description(permission.getDescription())
                        .build();

                permissionRepository.save(newPermission);

                inserted++;
                continue;
            }

            // 3. UPDATE DESCRIPTION IF CHANGED
            if (!Objects.equals(
                    dao.getDescription(),
                    permission.getDescription()
            )) {

                dao.setDescription(permission.getDescription());

                permissionRepository.save(dao);

                updated++;
            } else {
                unchanged++;
            }
        }

        // 4. FLUSH INSERTS AND UPDATES
        permissionRepository.flush();

        log.info(
                "Permission seeding completed. Inserted: {}, Updated: {}, Unchanged: {}",
                inserted,
                updated,
                unchanged
        );
    }
}
