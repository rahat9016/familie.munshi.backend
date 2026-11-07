package com.familiemunshi.common.service;

import com.familiemunshi.common.component.StorageFileSupport;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "storage.type",
        havingValue = "minio"
)
public class MinioStorageService implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final StorageFileSupport fileSupport;

    @PostConstruct
    public void initBucket() {
        try {
            String bucket = minioProperties.getBucketName();

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build()
                );

                log.info("MinIO bucket created: {}", bucket);
            }

        } catch (Exception e) {
            throw new StorageException(
                    "Failed to initialize MinIO bucket", e
            );
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {

        String objectKey = fileSupport.privateKey(file);
        return upload(file, objectKey);
    }

    @Override
    public String uploadPublicFile(
            MultipartFile file,
            String directory
    ) {

        String objectKey = fileSupport.publicKey(
                file,
                directory
        );

        return upload(file, objectKey);
    }

    private String upload(
            MultipartFile file,
            String objectKey
    ) {
        try (InputStream stream = file.getInputStream()) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .stream(
                                    stream,
                                    file.getSize(),
                                    -1L
                            )
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("Uploaded to MinIO: {}", objectKey);

            return objectKey;

        } catch (Exception e) {
            throw new StorageException(
                    "Failed to upload file to MinIO", e
            );
        }
    }

    @Override
    public InputStream downloadFile(String filename) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(filename)
                            .build()
            );

        } catch (Exception e) {
            throw new StorageException(
                    "Failed to download file from MinIO", e
            );
        }
    }

    @Override
    public String getPresignedUrl(
            String filename,
            int expiryInMinutes
    ) {
        if (expiryInMinutes < 1 || expiryInMinutes > 10080) {
            throw new IllegalArgumentException(
                    "Expiry must be between 1 and 10080 minutes"
            );
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(minioProperties.getBucketName())
                            .object(filename)
                            .expiry(
                                    expiryInMinutes,
                                    TimeUnit.MINUTES
                            )
                            .build()
            );

        } catch (Exception e) {
            throw new StorageException(
                    "Failed to generate MinIO URL", e
            );
        }
    }

    @Override
    public String getPublicUrl(String filename) {

        if (filename == null || !filename.startsWith("public/")) {
            throw new StorageException(
                    "File is not a public asset"
            );
        }

        return getPresignedUrl(filename, 60);
    }

    @Override
    public void deleteFile(String filename) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(filename)
                            .build()
            );

        } catch (Exception e) {
            throw new StorageException(
                    "Failed to delete file from MinIO", e
            );
        }
    }
}