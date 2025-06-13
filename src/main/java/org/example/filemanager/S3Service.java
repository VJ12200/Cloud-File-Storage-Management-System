package org.example.filemanager;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    /**
     * Uploads a MultipartFile directly to S3 without creating temporary files
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        logger.info("Original filename: {}", originalFilename);

        // Generate unique key name to avoid conflicts
        String keyName = generateUniqueKeyName(originalFilename);
        logger.info("Generated key name: {}", keyName);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .metadata(Collections.singletonMap("original-filename", originalFilename))
                    .build();

            logger.info("Uploading file with metadata: {}", putObjectRequest.metadata());

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return keyName;
        } catch (S3Exception e) {
            logger.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a presigned URL for downloading files (more secure than direct download)
     */
    public String generateDownloadUrl(String keyName) {
        try {
            // First check if file exists
            headObject(keyName);

            try (S3Presigner presigner = S3Presigner.create()) {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(keyName)
                        .build();

                GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofHours(1)) // URL expires in 1 hour
                        .getObjectRequest(getObjectRequest)
                        .build();

                PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
                return presignedRequest.url().toString();
            }
        } catch (NoSuchKeyException e) {
            throw new RuntimeException("File not found: " + keyName, e);
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to generate download URL: " + e.getMessage(), e);
        }
    }

    /**
     * Downloads file content as byte array (for direct streaming to client)
     */
    public byte[] downloadFileContent(String keyName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new RuntimeException("File not found: " + keyName, e);
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a file from S3
     */
    public boolean deleteFile(String keyName) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            s3Client.deleteObject(deleteRequest);
            return true;
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a file with the same original filename already exists
     */
    public String findExistingFileByOriginalName(String originalFilename) {
        try {
            List<S3Object> allFiles = listFiles();

            for (S3Object file : allFiles) {
                HeadObjectResponse metadata = getFileMetadata(file.key());
                String existingOriginalName = metadata.metadata().get("original-filename");

                if (originalFilename.equals(existingOriginalName)) {
                    logger.info("Found existing file with same original name: {} -> {}", originalFilename, file.key());
                    return file.key();
                }
            }

            logger.info("No existing file found with original name: {}", originalFilename);
            return null;
        } catch (Exception e) {
            logger.error("Error checking for existing file with original name: {}", originalFilename, e);
            return null;
        }
    }

    /**
     * Uploads file with option to replace existing file
     */
    public String uploadFileWithReplace(MultipartFile file, String existingKey) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        logger.info("Replacing existing file: {} with new content", existingKey);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(existingKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .metadata(Collections.singletonMap("original-filename", originalFilename))
                    .build();

            logger.info("Replacing file with metadata: {}", putObjectRequest.metadata());

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return existingKey;
        } catch (S3Exception e) {
            logger.error("Failed to replace file in S3", e);
            throw new RuntimeException("Failed to replace file in S3: " + e.getMessage(), e);
        }
    }

    /**
     * Gets file metadata
     */
    public HeadObjectResponse getFileMetadata(String keyName) {
        try {
            HeadObjectResponse response = headObject(keyName);
            logger.info("Retrieved metadata for key {}: {}", keyName, response.metadata());
            return response;
        } catch (NoSuchKeyException e) {
            logger.error("File not found: {}", keyName, e);
            throw new RuntimeException("File not found: " + keyName, e);
        }
    }

    /**
     * Lists all files in the S3 bucket
     */
    public List<S3Object> listFiles() {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            return response.contents();
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to list files: " + e.getMessage(), e);
        }
    }

    private HeadObjectResponse headObject(String keyName) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        return s3Client.headObject(headObjectRequest);
    }

    private String generateUniqueKeyName(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            String uuid = UUID.randomUUID().toString();
            logger.info("Generated UUID for empty filename: {}", uuid);
            return uuid;
        }

        // Get the file name and extension
        String fileName = originalFilename;
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");

        if (lastDotIndex > 0) {
            fileName = originalFilename.substring(0, lastDotIndex);
            extension = originalFilename.substring(lastDotIndex);
        }

        // Add a timestamp to ensure uniqueness while preserving the original name
        String timestamp = String.valueOf(System.currentTimeMillis());
        String finalName = fileName + "_" + timestamp + extension;
        logger.info("Generated unique name: {} from original: {}", finalName, originalFilename);
        return finalName;
    }
}