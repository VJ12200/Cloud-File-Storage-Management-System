# Spring Boot File Manager with AWS S3 Integration

A modern, secure file management application built with Spring Boot and AWS S3, featuring a responsive web interface for seamless file operations.

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green?style=flat-square&logo=spring)
![AWS S3](https://img.shields.io/badge/AWS-S3-orange?style=flat-square&logo=amazon-aws)

## Features

###  **Core Functionality**
- **Large File Support**: Upload files up to 500MB per file, 1GB per request
- **AWS S3 Integration**: Secure cloud storage with presigned URLs
- **File Conflict Resolution**: Smart handling of duplicate filenames with user choice
- **Real-time Progress**: Live upload progress tracking with visual feedback
- **File Search**: Quick search functionality across all uploaded files
- **Secure Downloads**: Presigned URLs for secure file access

### **Security Features**
- **Google OAuth2 Authentication**: Secure login with Google accounts
- **User Session Management**: Secure user sessions and profile display
- **No Hardcoded Credentials**: Uses AWS ProfileCredentialsProvider
- **Secure File Handling**: Proper validation 
- **Original Filename Preservation**: Maintains file metadata securely
- **Error Handling**: Comprehensive error management and logging

## Interface
- **Login Page** : Use Google to login
- **File Upload Area**: Click to Browse and Upload files or simply drag and drop to upload 
- **Logout Bar**: Press the logout button to logout and return back to the login page
- **File Search Bar**: Enter the file name to search
- **Uploaded Files**: Has options for both downloading the file to local storage or deleting it from the cloud storage. Also shows the File type , Size and Upload/Modified date

### Login Page
![Screenshot 2025-06-22 174037](https://github.com/user-attachments/assets/a5036f7c-043f-440d-8813-b8c7f79af540)

### Main Interface
![File Manager Interface](https://github.com/user-attachments/assets/27961c19-6082-48d8-b195-73584f2555b5)

### Single/Multipart File Upload with Progress
![Single](https://i.ibb.co/bjDkRhzv/Single-Upload.png)
![Multiple](https://i.ibb.co/B5tb7DZR/Multipart.png)

### Conflict Resolution
![Conflict Resolution](https://i.ibb.co/4QrwMQG/Conflict.png)

## Technology Stack

- **Backend**: Spring Boot 3.0+, Java 17+
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Cloud Storage**: AWS S3
- **Build Tool**: Gradle
- **Template Engine**: Thymeleaf

## Prerequisites

Before you begin, ensure you have the following installed:

-  **Java 17 or higher**
-  **Gradle 7.0+** (or use included wrapper)
-  **AWS Account** with S3 access
-  **Git** for cloning the repository

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/VJ12200/Cloud-File-Storage-Management-System
cd spring-boot-file-manager
```

### 2. Set Up Google OAuth2 Authentication

#### Create Google Cloud Project and OAuth2 Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Configure OAuth consent screen
5. Create OAuth2 credentials (Web application)
6. Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`

**For detailed setup instructions, see [GOOGLE_OAUTH_SETUP.md](GOOGLE_OAUTH_SETUP.md)**

#### Configure OAuth2 in Application

Update `src/main/resources/application.properties`:
```properties
# Replace with your actual Google OAuth2 credentials
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
```

### 3. Set Up AWS Credentials

Choose one of the following methods:

#### Option A: AWS Credentials File (Recommended)
Create `~/.aws/credentials` file:
```ini
[default]
aws_access_key_id = YOUR_ACCESS_KEY_ID
aws_secret_access_key = YOUR_SECRET_ACCESS_KEY
```

#### Option B: Environment Variables
```bash
export AWS_ACCESS_KEY_ID=your_access_key_id
export AWS_SECRET_ACCESS_KEY=your_secret_access_key
```

#### Option C: IAM Roles (For EC2 instances)
Attach an IAM role with S3 permissions to your EC2 instance.

### 4. Configure Application Properties

Update `src/main/resources/application.properties`:
```properties
# AWS Configuration
aws.bucket.name=your-s3-bucket-name
spring.cloud.aws.region.static=your-aws-region

# Optional: Customize file size limits
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=1GB
```

### 5. Create S3 Bucket

Create an S3 bucket in your AWS account:
```bash
aws s3 mb s3://your-bucket-name --region your-region
```

### 6. Run the Application

#### Using Gradle Wrapper (Recommended)
```bash
# Windows
./gradlew bootRun

# Linux/Mac
./gradlew bootRun
```

#### Using Gradle
```bash
gradle bootRun
```

#### Using JAR
```bash
./gradlew build
java -jar build/libs/file-manager-*.jar
```

### 7. Access the Application

Open your browser and navigate to:
```
http://localhost:8080
```

You will be redirected to the login page where you can authenticate with your Google account.

## Configuration Options

### File Upload Limits
```properties
# Maximum file size per upload
spring.servlet.multipart.max-file-size=500MB

# Maximum request size (for multiple files)
spring.servlet.multipart.max-request-size=1GB

# Server timeout for large uploads
server.tomcat.connection-timeout=300000
```

### AWS Configuration
```properties
# AWS Region
spring.cloud.aws.region.static=ap-southeast-1

# S3 Bucket Name
aws.bucket.name=your-bucket-name
```

### Logging Configuration
```properties
# Enable debug logging for AWS SDK
logging.level.software.amazon.awssdk=DEBUG

# Application logging
logging.level.org.example=DEBUG
```

##  API Endpoints

### Authentication Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/login` | Login page |
| `GET` | `/oauth2/authorization/google` | Initiate Google OAuth2 login |
| `POST` | `/logout` | Logout user |

### File Management Endpoints (Requires Authentication)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | Main file manager interface |
| `GET` | `/api/files` | List all files |
| `POST` | `/api/files/upload` | Upload new file |
| `GET` | `/api/files/download/{key}` | Download file |
| `DELETE` | `/api/files/{key}` | Delete file |
| `GET` | `/api/files/search?q={query}` | Search files |
| `GET` | `/api/files/status/{key}` | Check upload status |

## Deployment

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/file-manager-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### AWS EC2 Deployment
1. Launch EC2 instance with Java 17+
2. Attach IAM role with S3 permissions
3. Deploy JAR file and run

### Heroku Deployment
```bash
heroku create your-app-name
heroku config:set AWS_ACCESS_KEY_ID=your_key
heroku config:set AWS_SECRET_ACCESS_KEY=your_secret
git push heroku main
```


##  Troubleshooting
### Debug Mode
Enable debug logging:
```properties
logging.level.org.example=DEBUG
logging.level.software.amazon.awssdk=DEBUG
```
[Document.txt](Document.txt) includes and explanation of most of the features along with issues I faced in development and their solutions.

</div>
