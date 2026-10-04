package com.familiemunshi.service;

import com.familiemunshi.common.configs.FileStorageProperties;
import com.familiemunshi.common.configs.MinioProperties;
import com.familiemunshi.common.exceptions.StorageException;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final FileStorageProperties fileProperties;

    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
                log.info("Successfully created MinIO bucket: {}", minioProperties.getBucketName());
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket: {}", minioProperties.getBucketName(), e);
            throw new StorageException("Failed to initialize storage bucket", e);
        }
    }

    public String uploadFile(MultipartFile file) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        String filename = UUID.randomUUID() + "_" + sanitizedFilename;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(filename)
                            .stream(inputStream, file.getSize(), -1L)
                            .contentType(file.getContentType())
                            .build());
            return filename;
        } catch (Exception e) {
            log.error("Error uploading file to MinIO", e);
            throw new StorageException("Failed to upload file", e);
        }
    }

    public InputStream downloadFile(String filename) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(filename)
                            .build());
        } catch (Exception e) {
            log.error("Error downloading file from MinIO: {}", filename, e);
            throw new StorageException("Failed to download file", e);
        }
    }

    public String getPresignedUrl(String filename, int expiryInMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(minioProperties.getBucketName())
                            .object(filename)
                            .expiry(expiryInMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            log.error("Error generating presigned URL for file: {}", filename, e);
            throw new StorageException("Failed to generate download URL", e);
        }
    }

    public void deleteFile(String filename) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(filename)
                            .build());
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", filename, e);
            throw new StorageException("Failed to delete file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Cannot upload empty or null file");
        }

        if (file.getSize() > fileProperties.getMaxSizeBytes()) {
            throw new StorageException("File size exceeds maximum permitted size of " + fileProperties.getMaxSizeBytes() + " bytes");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!fileProperties.getAllowedExtensions().contains(extension)) {
                throw new StorageException("File extension '" + extension + "' is not allowed");
            }
        }
    }
}