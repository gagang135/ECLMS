package com.company.eclms.common.storage;

import java.io.InputStream;

public interface StorageService {
    String uploadFile(String bucketName, String objectName, InputStream content, long size, String contentType);
    InputStream downloadFile(String bucketName, String objectName);
    String getPreviewUrl(String bucketName, String objectName);
    void deleteFile(String bucketName, String objectName);
}
