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

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class PermissionSeeder implements ApplicationRunner {
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args){
        int inserted = 0;
        for (PermissionCode permission : PermissionCode.values()) {

            if (permissionRepository.existsByName(
                    permission.getCode()
            )) {
                continue;
            }

            PermissionDao dao = PermissionDao.builder()
                    .name(permission.getCode())
                    .build();

            permissionRepository.save(dao);
            inserted++;
        }

        log.info(
                "Permission seeding completed. Inserted: {}",
                inserted
        );
    }
}
