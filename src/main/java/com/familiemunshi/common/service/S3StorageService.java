
package com.familiemunshi.common.service;

import com.familiemunshi.common.component.StorageFileSupport;
import com.familiemunshi.common.configs.S3Properties;
import com.familiemunshi.common.exceptions.StorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "storage.type",
        havingValue = "s3"
)
public class S3StorageService implements FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;
    private final StorageFileSupport fileSupport;

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

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(objectKey)
                    .contentType(
                            file.getContentType() == null
                                    ? "application/octet-stream"
                                    : file.getContentType()
                    )
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            stream,
                            file.getSize()
                    )
            );

            log.info("Uploaded file to S3: {}", objectKey);

            return objectKey;

        } catch (Exception e) {

            log.error("S3 upload failed: {}", objectKey, e);

            throw new StorageException(
                    "Failed to upload file to S3",
                    e
            );
        }
    }

    @Override
    public InputStream downloadFile(String filename) {

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(filename)
                    .build();

            return s3Client.getObject(request);

        } catch (Exception e) {

            throw new StorageException(
                    "Failed to download file from S3",
                    e
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
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(filename)
                    .build();

            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(
                                    Duration.ofMinutes(expiryInMinutes)
                            )
                            .getObjectRequest(request)
                            .build();

            return s3Presigner.presignGetObject(
                    presignRequest
            ).url().toString();

        } catch (Exception e) {

            throw new StorageException(
                    "Failed to generate S3 presigned URL",
                    e
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

        String baseUrl = properties.getPublicBaseUrl();

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new StorageException(
                    "S3 public URL base is not configured"
            );
        }

        return baseUrl.replaceAll("/+$", "")
                + "/"
                + filename;
    }

    @Override
    public void deleteFile(String filename) {

        try {
            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .bucket(properties.getBucketName())
                            .key(filename)
                            .build();

            s3Client.deleteObject(request);

            log.info("Deleted S3 object: {}", filename);

        } catch (Exception e) {

            log.error("S3 deletion failed: {}", filename, e);

            throw new StorageException(
                    "Failed to delete file from S3",
                    e
            );
        }
    }
}
