package org.example.filemanager.config;

import org.example.filemanager.S3Service;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test configuration to mock AWS services and other external dependencies
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public S3Service mockS3Service() {
        return new S3Service() {
            @Override
            public List<S3Object> listFiles() {
                // Return empty list for tests
                return new ArrayList<>();
            }

            @Override
            public String uploadFile(MultipartFile file) {
                // Mock upload - just return a test key
                return "test-file-key";
            }

            @Override
            public String uploadFileWithReplace(MultipartFile file, String existingKey) {
                // Mock upload with replace
                return existingKey;
            }

            @Override
            public boolean deleteFile(String key) {
                // Mock delete - always return true
                return true;
            }

            @Override
            public byte[] downloadFileContent(String key) {
                // Mock download - return test content
                return "test file content".getBytes();
            }

            @Override
            public String generateDownloadUrl(String key) {
                // Mock URL generation
                return "https://test-bucket.s3.amazonaws.com/" + key;
            }

            @Override
            public String findExistingFileByOriginalName(String originalFilename) {
                // Mock - return null (no existing files)
                return null;
            }

            @Override
            public HeadObjectResponse getFileMetadata(String keyName) {
                // Mock metadata response
                Map<String, String> metadata = new HashMap<>();
                metadata.put("original-filename", "test-file.txt");
                return HeadObjectResponse.builder()
                    .metadata(metadata)
                    .contentLength(100L)
                    .build();
            }
        };
    }
}
