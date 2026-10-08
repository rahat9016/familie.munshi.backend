package com.familiemunshi.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {
    String uploadFile(MultipartFile file);

    InputStream downloadFile(String filename);

    String getPresignedUrl(String filename, int expiryInMinutes);

    void deleteFile(String filename);
}