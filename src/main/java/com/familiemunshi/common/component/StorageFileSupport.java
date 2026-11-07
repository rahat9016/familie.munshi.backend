package com.familiemunshi.common.component;

import com.familiemunshi.common.configs.FileStorageProperties;
import com.familiemunshi.common.exceptions.StorageException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StorageFileSupport {

    // Allowed public storage directories
    public static final String BRANCH_LOGOS = "branches/logos";
    public static final String COMPANY_LOGOS = "company/logos";
    public static final String PRODUCT_IMAGES = "products/images";

    private static final Set<String> PUBLIC_DIRECTORIES = Set.of(
            BRANCH_LOGOS,
            COMPANY_LOGOS,
            PRODUCT_IMAGES
    );

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp"
    );

    private final FileStorageProperties properties;

    public void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new StorageException("File cannot be empty");
        }

        if (file.getSize() > properties.getMaxSizeBytes()) {
            throw new StorageException(
                    "File exceeds maximum permitted size"
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.contains(".")) {
            throw new StorageException(
                    "File extension is required"
            );
        }

        String extension = extensionOf(filename);

        if (!properties.getAllowedExtensions().contains(extension)) {
            throw new StorageException(
                    "File extension is not allowed: " + extension
            );
        }
    }

    public String privateKey(MultipartFile file) {

        validate(file);

        return "private/"
                + UUID.randomUUID()
                + "."
                + extensionOf(
                Objects.requireNonNull(file.getOriginalFilename())
        );
    }

    public String publicKey(
            MultipartFile file,
            String directory
    ) {

        validate(file);

        if (directory == null || !PUBLIC_DIRECTORIES.contains(directory)) {
            throw new StorageException(
                    "Invalid public storage directory: " + directory
            );
        }

        String extension = extensionOf(
                Objects.requireNonNull(file.getOriginalFilename())
        );

        if (!IMAGE_EXTENSIONS.contains(extension)) {
            throw new StorageException(
                    "Public assets must be images"
            );
        }

        return "public/"
                + directory
                + "/"
                + UUID.randomUUID()
                + "."
                + extension;
    }

    private String extensionOf(String filename) {

        return filename.substring(
                filename.lastIndexOf(".") + 1
        ).toLowerCase(Locale.ROOT);
    }
}