# Spring Boot File Manager with AWS S3 Integration

A modern, secure file management application built with Spring Boot and AWS S3, featuring a responsive web interface for seamless file operations.

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green?style=flat-square&logo=spring)
![AWS S3](https://img.shields.io/badge/AWS-S3-orange?style=flat-square&logo=amazon-aws)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=flat-square&logo=docker)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-green?style=flat-square&logo=github-actions)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

## Table of Contents

- [Features](#features)
- [Interface](#interface)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration Options](#configuration-options)
- [API Endpoints](#api-endpoints)
- [CI/CD Pipeline](#cicd-pipeline)
- [Documentation](#documentation)

## Features

### **Core Functionality**
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

## CI/CD Pipeline

This project includes a comprehensive CI/CD pipeline using GitHub Actions, Docker, and automated testing.

### GitHub Actions Workflow

The project uses GitHub Actions for continuous integration and deployment. The workflow is triggered on:
- Push to `main` or `master` branches
- Pull requests to any branch

#### Workflow Steps

1. **Code Checkout**: Uses `actions/checkout@v4` to fetch the latest code
2. **Java Setup**: Configures OpenJDK 17 with Temurin distribution
3. **Gradle Caching**: Caches Gradle dependencies for faster builds
4. **Environment Setup**: Creates test environment with dummy credentials
5. **Testing**: Runs comprehensive test suite with `./gradlew test`
6. **Build**: Compiles and packages the application with `./gradlew build`
7. **Artifact Upload**: Stores build artifacts for deployment

#### Workflow Configuration

<augment_code_snippet path=".github/workflows/ci.yml" mode="EXCERPT">
````yaml
name: CI/CD Pipeline

on:
  push:
    branches:
      - main
      - master
  pull_request:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
    # ... additional steps
````
</augment_code_snippet>

### Docker Containerization

The application is fully containerized with Docker support for consistent deployments across environments.

#### Docker Features

- **Multi-stage builds** for optimized image size
- **Non-root user** for enhanced security
- **Health checks** for container monitoring
- **Environment variable** support for configuration

#### Building Docker Image

```bash
# Build the application first
./gradlew build

# Build Docker image
docker build -t file-manager:latest .

# Run container
docker run -p 8080:8080 \
  -e AWS_ACCESS_KEY_ID=your_key \
  -e AWS_SECRET_ACCESS_KEY=your_secret \
  -e AWS_DEFAULT_REGION=your_region \
  file-manager:latest
```

#### Docker Compose Deployment

For local development and testing:

```bash
# Start the application with Docker Compose
docker-compose up -d

# Start with LocalStack for S3 testing
docker-compose --profile local-testing up -d

# View logs
docker-compose logs -f file-manager

# Stop services
docker-compose down
```

### Environment Configuration

#### CI/CD Environment Variables

Set the following secrets in your GitHub repository settings:

| Secret Name | Description | Required |
|-------------|-------------|----------|
| `AWS_ACCESS_KEY_ID` | AWS access key for S3 operations | Yes |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key for S3 operations | Yes |
| `AWS_DEFAULT_REGION` | AWS region for S3 bucket | Yes |
| `S3_BUCKET_NAME` | Name of your S3 bucket | Yes |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | Yes |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | Yes |

#### Local Development with LocalStack

For local S3 testing without AWS costs:

```bash
# Start LocalStack S3 service
docker-compose --profile local-testing up localstack -d

# Configure AWS CLI for LocalStack
aws configure set aws_access_key_id test
aws configure set aws_secret_access_key test
aws configure set default.region us-east-1

# Create test bucket
aws --endpoint-url=http://localhost:4566 s3 mb s3://test-bucket

# Run application with LocalStack
export AWS_ENDPOINT_URL=http://localhost:4566
./gradlew bootRun
```

### Documentation

- **Feature Documentation**: See `src/Documentation/` for detailed feature explanations
- **Development Issues**: Check `Document.txt` for common development issues and solutions
- **AWS S3 Setup**: Refer to AWS S3 documentation for bucket configuration
- **Google OAuth2**: See `GOOGLE_OAUTH_SETUP.md` for detailed OAuth2 setup instructions


