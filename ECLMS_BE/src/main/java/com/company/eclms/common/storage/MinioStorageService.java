package com.company.eclms.common.storage;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioStorageService implements StorageService {

    @Value("${app.storage.minio.endpoint}")
    private String endpoint;

    @Value("${app.storage.minio.access-key}")
    private String accessKey;

    @Value("${app.storage.minio.secret-key}")
    private String secretKey;

    @Value("${app.storage.minio.bucket-name}")
    private String defaultBucket;

    private MinioClient minioClient;
    private boolean fallbackMode = false;
    private Path fallbackDir;

    @PostConstruct
    public void init() {
        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // Test connection by checking if default bucket exists
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(defaultBucket).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(defaultBucket).build()
                );
            }
            log.info("Successfully connected to MinIO and initialized bucket: {}", defaultBucket);
        } catch (Exception e) {
            log.warn("MinIO failed to initialize. Falling back to local file storage. Error: {}", e.getMessage());
            this.fallbackMode = true;
            this.fallbackDir = Paths.get("logs", "minio-fallback");
            try {
                Files.createDirectories(fallbackDir);
            } catch (IOException ioException) {
                log.error("Failed to create local fallback directory", ioException);
            }
        }
    }

    @Override
    public String uploadFile(String bucketName, String objectName, InputStream content, long size, String contentType) {
        String bucket = (bucketName != null) ? bucketName : defaultBucket;
        if (fallbackMode) {
            log.info("LOCAL STORAGE - Uploading file: {}/{}", bucket, objectName);
            Path filePath = fallbackDir.resolve(objectName);
            try {
                Files.createDirectories(filePath.getParent() != null ? filePath.getParent() : fallbackDir);
                try (OutputStream os = Files.newOutputStream(filePath)) {
                    content.transferTo(os);
                }
                return filePath.toAbsolutePath().toString();
            } catch (IOException e) {
                throw new RuntimeException("Local storage upload failed", e);
            }
        }

        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(content, size, -1)
                            .contentType(contentType)
                            .build()
            );
            return bucket + "/" + objectName;
        } catch (Exception e) {
            log.error("MinIO upload failed", e);
            throw new RuntimeException("MinIO upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadFile(String bucketName, String objectName) {
        String bucket = (bucketName != null) ? bucketName : defaultBucket;
        if (fallbackMode) {
            log.info("LOCAL STORAGE - Downloading file: {}/{}", bucket, objectName);
            Path filePath = fallbackDir.resolve(objectName);
            try {
                return Files.newInputStream(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Local storage download failed", e);
            }
        }

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO download failed", e);
            throw new RuntimeException("MinIO download failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPreviewUrl(String bucketName, String objectName) {
        String bucket = (bucketName != null) ? bucketName : defaultBucket;
        if (fallbackMode) {
            Path filePath = fallbackDir.resolve(objectName);
            return "file:///" + filePath.toAbsolutePath().toString().replace("\\", "/");
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(2, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO pre-signed URL generation failed", e);
            throw new RuntimeException("MinIO preview URL failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String bucketName, String objectName) {
        String bucket = (bucketName != null) ? bucketName : defaultBucket;
        if (fallbackMode) {
            log.info("LOCAL STORAGE - Deleting file: {}/{}", bucket, objectName);
            Path filePath = fallbackDir.resolve(objectName);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.error("Local file delete failed", e);
            }
            return;
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO delete failed", e);
            throw new RuntimeException("MinIO delete failed: " + e.getMessage(), e);
        }
    }
}
