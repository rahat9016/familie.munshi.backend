package com.familiemunshi.common.configs;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadDir = "uploads";
    private List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "webp", "pdf");
    private long maxSizeBytes = 10 * 1024 * 1024;
}
