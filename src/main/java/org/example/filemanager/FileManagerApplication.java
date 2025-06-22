package org.example.filemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileManagerApplication {
    private static final Logger logger = LoggerFactory.getLogger(FileManagerApplication.class);

    public static void main(String[] args) {
        // Log environment variables for debugging
        logger.info("=== Environment Variables Debug ===");
        logger.info("GOOGLE_CLIENT_ID from env: {}", System.getenv("GOOGLE_CLIENT_ID"));
        logger.info("GOOGLE_CLIENT_SECRET from env: {} (length: {})",
                   System.getenv("GOOGLE_CLIENT_SECRET") != null ?
                   System.getenv("GOOGLE_CLIENT_SECRET").substring(0, Math.min(10, System.getenv("GOOGLE_CLIENT_SECRET").length())) + "..." : "null",
                   System.getenv("GOOGLE_CLIENT_SECRET") != null ? System.getenv("GOOGLE_CLIENT_SECRET").length() : 0);
        logger.info("=== End Environment Variables Debug ===");

        SpringApplication.run(FileManagerApplication.class, args);
    }
}