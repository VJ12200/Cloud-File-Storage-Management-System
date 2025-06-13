# 🗂️ Spring Boot File Manager with AWS S3 Integration

A modern, secure file management application built with Spring Boot and AWS S3, featuring a responsive web interface for seamless file operations.

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green?style=flat-square&logo=spring)
![AWS S3](https://img.shields.io/badge/AWS-S3-orange?style=flat-square&logo=amazon-aws)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

## ✨ Features

### 🚀 **Core Functionality**
- **Large File Support**: Upload files up to 500MB per file, 1GB per request
- **AWS S3 Integration**: Secure cloud storage with presigned URLs
- **File Conflict Resolution**: Smart handling of duplicate filenames with user choice
- **Real-time Progress**: Live upload progress tracking with visual feedback
- **File Search**: Quick search functionality across all uploaded files
- **Secure Downloads**: Presigned URLs for secure file access

### 🎨 **Modern User Interface**
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile
- **Dark Theme**: Modern dark UI with gradient effects and animations
- **Drag & Drop**: Intuitive file upload with drag-and-drop support
- **Glass Morphism**: Beautiful glass effects and smooth transitions
- **Toast Notifications**: Real-time feedback for all operations

### 🔒 **Security Features**
- **No Hardcoded Credentials**: Uses AWS ProfileCredentialsProvider
- **Secure File Handling**: Proper validation and sanitization
- **Original Filename Preservation**: Maintains file metadata securely
- **Error Handling**: Comprehensive error management and logging

## 🖼️ Screenshots

### Main Interface
![File Manager Interface](https://via.placeholder.com/800x500/1a1a2e/64ffda?text=Modern+File+Manager+Interface)

### File Upload with Progress
![Upload Progress](https://via.placeholder.com/800x300/1a1a2e/64ffda?text=Upload+Progress+Tracking)

### Conflict Resolution
![Conflict Resolution](https://via.placeholder.com/600x400/1a1a2e/64ffda?text=File+Conflict+Resolution)

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.0+, Java 17+
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Cloud Storage**: AWS S3
- **Build Tool**: Gradle
- **Template Engine**: Thymeleaf

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- ☕ **Java 17 or higher**
- 🐘 **Gradle 7.0+** (or use included wrapper)
- 🌐 **AWS Account** with S3 access
- 🔧 **Git** for cloning the repository

## ⚡ Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/spring-boot-file-manager.git
cd spring-boot-file-manager
```

### 2. Set Up AWS Credentials

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

### 3. Configure Application Properties

Update `src/main/resources/application.properties`:
```properties
# AWS Configuration
aws.bucket.name=your-s3-bucket-name
spring.cloud.aws.region.static=your-aws-region

# Optional: Customize file size limits
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=1GB
```

### 4. Create S3 Bucket

Create an S3 bucket in your AWS account:
```bash
aws s3 mb s3://your-bucket-name --region your-region
```

### 5. Run the Application

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

### 6. Access the Application

Open your browser and navigate to:
```
http://localhost:8080
```

## 🔧 Configuration Options

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

## 📚 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | Main file manager interface |
| `GET` | `/api/files` | List all files |
| `POST` | `/api/files/upload` | Upload new file |
| `GET` | `/api/files/download/{key}` | Download file |
| `DELETE` | `/api/files/{key}` | Delete file |
| `GET` | `/api/files/search?q={query}` | Search files |
| `GET` | `/api/files/status/{key}` | Check upload status |

## 🎯 Usage Examples

### Upload Files
1. **Drag & Drop**: Drag files directly onto the upload area
2. **Click to Browse**: Click the upload area to select files
3. **Multiple Files**: Select multiple files for batch upload
4. **Progress Tracking**: Watch real-time upload progress

### Handle File Conflicts
When uploading a file with an existing name:
1. **Cancel**: Abort the upload
2. **Replace**: Overwrite the existing file
3. **Keep Both**: Upload with a unique name

### Search Files
Use the search bar to find files by:
- Original filename
- File extension
- Partial name matches

## 🚀 Deployment

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

## 🔒 Security Best Practices

### ✅ **Implemented Security Measures**
- No hardcoded AWS credentials
- Secure credential provider chain
- Input validation and sanitization
- Presigned URLs for secure downloads
- Comprehensive error handling

### 🛡️ **Additional Recommendations**
- Use HTTPS in production
- Implement rate limiting
- Regular security audits
- Monitor AWS CloudTrail logs
- Set up S3 bucket policies

## 🐛 Troubleshooting

### Common Issues

#### AWS Credentials Not Found
```
Error: Unable to load AWS credentials
```
**Solution**: Verify AWS credentials are properly configured using one of the methods above.

#### S3 Bucket Access Denied
```
Error: Access Denied (Service: Amazon S3)
```
**Solution**: Check IAM permissions and bucket policies.

#### File Upload Timeout
```
Error: Upload timeout
```
**Solution**: Increase server timeout or check network connection.

#### Large File Upload Fails
```
Error: File too large
```
**Solution**: Verify file size limits in `application.properties`.

### Debug Mode
Enable debug logging:
```properties
logging.level.org.example=DEBUG
logging.level.software.amazon.awssdk=DEBUG
```

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Setup
```bash
git clone https://github.com/yourusername/spring-boot-file-manager.git
cd spring-boot-file-manager
./gradlew build
./gradlew test
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Spring Boot Team** for the excellent framework
- **AWS** for reliable cloud storage services
- **Community Contributors** for feedback and improvements

## 📞 Support

- 📧 **Email**: your.email@example.com
- 🐛 **Issues**: [GitHub Issues](https://github.com/yourusername/spring-boot-file-manager/issues)
- 📖 **Documentation**: [Wiki](https://github.com/yourusername/spring-boot-file-manager/wiki)

## 🗺️ Roadmap

### 🔮 **Planned Features**
- [ ] User authentication and authorization
- [ ] File sharing with expirable links
- [ ] File versioning and history
- [ ] Batch operations (zip/unzip)
- [ ] File preview functionality
- [ ] Advanced search filters
- [ ] File encryption at rest
- [ ] Mobile app version

### 🎯 **Performance Improvements**
- [ ] CDN integration
- [ ] Caching layer
- [ ] Database integration for metadata
- [ ] WebSocket support for real-time updates

---

<div align="center">

**⭐ Star this repository if you find it helpful!**

Made with ❤️ by [Your Name](https://github.com/yourusername)

</div>
