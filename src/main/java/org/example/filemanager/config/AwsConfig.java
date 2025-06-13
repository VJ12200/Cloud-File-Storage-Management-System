

package org.example.filemanager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 Using AWS S3 in springboot without hardcoding aws credentials, recommended for production use.
 Here AWS SDK's default credentials provider chain searches for the credentials in the following sequence
 1. Environment Variable
 2. Java System Properties
 3. AWS Credentials file (Stored locally in C:\Users\YOUR_USERNAME\.aws\credentials )
 **/
@Configuration
public class AwsConfig {
    private static final Logger logger = LoggerFactory.getLogger(AwsConfig.class);

    @Bean
    public S3Client s3Client() {
        logger.info("Initializing S3Client with ProfileCredentialsProvider");

        // Use the "default" profile from the AWS credentials file
        // This reads from ~/.aws/credentials (or C:\Users\USERNAME\.aws\credentials on Windows)
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("default");

        return S3Client.builder()
                .region(Region.AP_SOUTHEAST_1) // Replace with your region if needed
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
