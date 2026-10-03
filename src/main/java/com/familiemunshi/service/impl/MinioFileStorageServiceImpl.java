//package com.familiemunshi.service.impl;
//
//import com.familiemunshi.exception.StorageException;
//import com.familiemunshi.service.FileStorageService;
//import io.minio.*;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.InputStream;
//import java.util.UUID;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
//public class MinioFileStorageServiceImpl implements FileStorageService {
//
//    private final MinioClient minioClient;
//
//    @Value("${storage.minio.bucket-name}")
//    private String bucketName;
//
//    @Override
//    public String storeFile(MultipartFile file, String folder) {
//        try {
//            ensureBucketExists();
//
//            String originalFilename = file.getOriginalFilename();
//            String extension = originalFilename != null && originalFilename.contains(".")
//                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
//                    : "";
//
//            String objectName = (folder != null && !folder.isBlank() ? folder + "/" : "")
//                    + UUID.randomUUID() + extension;
//
//            try (InputStream inputStream = file.getInputStream()) {
//                minioClient.putObject(
//                        PutObjectArgs.builder()
//                                .bucket(bucketName)
//                                .object(objectName)
//                                .stream(inputStream, file.getSize(), -1)
//                                .contentType(file.getContentType())
//                                .build()
//                );
//            }
//
//            log.info("File successfully uploaded to MinIO: {}", objectName);
//            return objectName;
//        } catch (Exception e) {
//            log.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
//            throw new StorageException("Failed to upload file to MinIO storage", e);
//        }
//    }
//
//    @Override
//    public byte[] downloadFile(String fileName) {
//        try (InputStream stream = minioClient.getObject(
//                GetObjectArgs.builder()
//                        .bucket(bucketName)
//                        .object(fileName)
//                        .build())) {
//            return stream.readAllBytes();
//        } catch (Exception e) {
//            log.error("Failed to download file from MinIO: {}", e.getMessage(), e);
//            throw new StorageException("Failed to download file from MinIO", e);
//        }
//    }
//
//    @Override
//    public void deleteFile(String fileName) {
//        try {
//            minioClient.removeObject(
//                    RemoveObjectArgs.builder()
//                            .bucket(bucketName)
//                            .object(fileName)
//                            .build()
//            );
//            log.info("File deleted from MinIO: {}", fileName);
//        } catch (Exception e) {
//            log.error("Failed to delete file from MinIO: {}", e.getMessage(), e);
//            throw new StorageException("Failed to delete file from MinIO", e);
//        }
//    }
//
//    private void ensureBucketExists() throws Exception {
//        boolean exists = minioClient.bucketExists(
//                BucketExistsArgs.builder().bucket(bucketName).build()
//        );
//        if (!exists) {
//            minioClient.makeBucket(
//                    MakeBucketArgs.builder().bucket(bucketName).build()
//            );
//            log.info("MinIO bucket '{}' created successfully", bucketName);
//        }
//    }
//}