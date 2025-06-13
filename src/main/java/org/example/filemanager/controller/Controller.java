package org.example.filemanager.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.example.filemanager.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

@RestController
@RequestMapping("/api")
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @Autowired
    private S3Service s3Service;

    // Map to store upload status
    private final ConcurrentHashMap<String, Boolean> uploadStatus = new ConcurrentHashMap<>();

    @GetMapping("/files")
    public ResponseEntity<?> listFiles() {
        try {
            List<S3Object> files = s3Service.listFiles();
            logger.info("Retrieved {} files from S3", files.size());

            List<Map<String, Object>> fileList = files.stream()
                .map(file -> {
                    Map<String, Object> fileInfo = new HashMap<>();
                    String key = file.key();
                    logger.info("Processing file with key: {}", key);

                    // Get file metadata to retrieve original filename
                    HeadObjectResponse metadata = s3Service.getFileMetadata(key);
                    String originalName = metadata.metadata().get("original-filename");
                    logger.info("Retrieved original name from metadata: {}", originalName);

                    if (originalName == null) {
                        // Fallback to extracting from key if metadata is not available
                        int lastUnderscoreIndex = key.lastIndexOf("_");
                        if (lastUnderscoreIndex > 0) {
                            originalName = key.substring(0, lastUnderscoreIndex);
                        } else {
                            originalName = key;
                        }
                        logger.info("Using fallback original name: {}", originalName);
                    }

                    fileInfo.put("key", key);
                    fileInfo.put("originalName", originalName);
                    fileInfo.put("size", file.size());
                    fileInfo.put("lastModified", file.lastModified());
                    fileInfo.put("downloadUrl", s3Service.generateDownloadUrl(key));

                    logger.info("File info: {}", fileInfo);
                    return fileInfo;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(fileList);
        } catch (Exception e) {
            logger.error("Failed to list files", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Failed to list files: " + e.getMessage())
            );
        }
    }

    @PostMapping("/files/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Please select a file to upload")
            );
        }

        String originalFilename = file.getOriginalFilename();

        // Check if file with same original name already exists
        String existingKey = s3Service.findExistingFileByOriginalName(originalFilename);

        if (existingKey != null) {
            // File already exists, return conflict response with options
            logger.info("File conflict detected for: {} (existing key: {})", originalFilename, existingKey);
            assert originalFilename != null;
            return ResponseEntity.status(409).body(Map.of(
                "conflict", true,
                "message", "A file with the same name already exists",
                "originalFilename", originalFilename,
                "existingKey", existingKey,
                "options", Map.of(
                    "cancel", "Cancel the upload",
                    "replace", "Replace the existing file",
                    "keepBoth", "Keep both files (new file will have a unique name)"
                )
            ));
        }

        try {
            String keyName = s3Service.uploadFile(file);
            // Mark upload as completed
            uploadStatus.put(keyName, true);

            return ResponseEntity.ok(Map.of(
                "message", "File uploaded successfully",
                "key", keyName,
                "downloadUrl", s3Service.generateDownloadUrl(keyName)
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Failed to upload file: " + e.getMessage())
            );
        }
    }

    @PostMapping("/files/upload/resolve-conflict")
    public ResponseEntity<?> resolveUploadConflict(
            @RequestParam("file") MultipartFile file,
            @RequestParam("action") String action,
            @RequestParam(value = "existingKey", required = false) String existingKey) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Please select a file to upload")
            );
        }

        try {
            String keyName;
            String message;

            switch (action.toLowerCase()) {
                case "cancel":
                    return ResponseEntity.ok(Map.of(
                        "message", "Upload cancelled by user",
                        "cancelled", true
                    ));

                case "replace":
                    if (existingKey == null || existingKey.isEmpty()) {
                        return ResponseEntity.badRequest().body(
                            Map.of("error", "Existing key is required for replace action")
                        );
                    }
                    keyName = s3Service.uploadFileWithReplace(file, existingKey);
                    message = "File replaced successfully";
                    break;

                case "keepboth":
                    keyName = s3Service.uploadFile(file); // This will generate a unique name
                    message = "File uploaded with unique name (both files kept)";
                    break;

                default:
                    return ResponseEntity.badRequest().body(
                        Map.of("error", "Invalid action. Must be 'cancel', 'replace', or 'keepBoth'")
                    );
            }

            // Mark upload as completed
            uploadStatus.put(keyName, true);

            return ResponseEntity.ok(Map.of(
                "message", message,
                "key", keyName,
                "downloadUrl", s3Service.generateDownloadUrl(keyName),
                "action", action
            ));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Failed to upload file: " + e.getMessage())
            );
        }
    }

    @GetMapping("/files/download/{key}")
    public ResponseEntity<?> downloadFile(@PathVariable String key) {
        try {
            byte[] fileContent = s3Service.downloadFileContent(key);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + key + "\"")
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Failed to download file: " + e.getMessage())
            );
        }
    }

    @DeleteMapping("/files/{key}")
    public ResponseEntity<?> deleteFile(@PathVariable String key) {
        try {
            boolean deleted = s3Service.deleteFile(key);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Failed to delete file: " + e.getMessage())
            );
        }
    }

    @GetMapping("/files/status/{key}")
    public ResponseEntity<?> getUploadStatus(@PathVariable String key) {
        boolean completed = uploadStatus.containsKey(key);

        if (completed) {
            // Clean up the status after checking
            uploadStatus.remove(key);
        }

        return ResponseEntity.ok(Map.of("completed", completed));
    }

    @GetMapping("/files/search")
    public ResponseEntity<?> searchFiles(@RequestParam("q") String query) {
        try {
            List<S3Object> allFiles = s3Service.listFiles();
            logger.info("Searching for '{}' in {} files from S3", query, allFiles.size());

            List<Map<String, Object>> matchingFiles = allFiles.stream()
                .filter(file -> {
                    String key = file.key();
                    // Get file metadata to retrieve original filename
                    HeadObjectResponse metadata = s3Service.getFileMetadata(key);
                    String originalName = metadata.metadata().get("original-filename");

                    if (originalName == null) {
                        // Fallback to extracting from key if metadata is not available
                        int lastUnderscoreIndex = key.lastIndexOf("_");
                        if (lastUnderscoreIndex > 0) {
                            originalName = key.substring(0, lastUnderscoreIndex);
                        } else {
                            originalName = key;
                        }
                    }

                    // Search in both original filename and S3 key (case-insensitive)
                    return originalName.toLowerCase().contains(query.toLowerCase()) ||
                           key.toLowerCase().contains(query.toLowerCase());
                })
                .map(file -> {
                    Map<String, Object> fileInfo = new HashMap<>();
                    String key = file.key();

                    // Get file metadata to retrieve original filename
                    HeadObjectResponse metadata = s3Service.getFileMetadata(key);
                    String originalName = metadata.metadata().get("original-filename");

                    if (originalName == null) {
                        // Fallback to extracting from key if metadata is not available
                        int lastUnderscoreIndex = key.lastIndexOf("_");
                        if (lastUnderscoreIndex > 0) {
                            originalName = key.substring(0, lastUnderscoreIndex);
                        } else {
                            originalName = key;
                        }
                    }

                    fileInfo.put("key", key);
                    fileInfo.put("originalName", originalName);
                    fileInfo.put("size", file.size());
                    fileInfo.put("lastModified", file.lastModified());
                    fileInfo.put("downloadUrl", s3Service.generateDownloadUrl(key));

                    return fileInfo;
                })
                .collect(Collectors.toList());

            logger.info("Found {} matching files for query '{}'", matchingFiles.size(), query);
            return ResponseEntity.ok(matchingFiles);
        } catch (Exception e) {
            logger.error("Failed to search files for query '{}'", query, e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Failed to search files: " + e.getMessage())
            );
        }
    }
}
