# Google OAuth2 Setup Guide

This guide will help you set up Google OAuth2 authentication for your File Manager application.

## Prerequisites

- Google account
- Access to Google Cloud Console
- Your application running on a known URL (localhost for development)

## Step 1: Create a Google Cloud Project

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Click on the project dropdown and select "New Project"
3. Enter a project name (e.g., "File Manager App")
4. Click "Create"

## Step 2: Enable Google+ API

1. In the Google Cloud Console, go to "APIs & Services" > "Library"
2. Search for "Google+ API" 
3. Click on it and press "Enable"
4. Also enable "Google OAuth2 API" if available

## Step 3: Configure OAuth Consent Screen

1. Go to "APIs & Services" > "OAuth consent screen"
2. Choose "External" user type (unless you have a Google Workspace account)
3. Fill in the required information:
   - **App name**: File Manager
   - **User support email**: Your email
   - **Developer contact information**: Your email
4. Add scopes:
   - `openid`
   - `profile` 
   - `email`
5. Add test users (your email and any other emails you want to test with)
6. Save and continue

## Step 4: Create OAuth2 Credentials

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth client ID"
3. Choose "Web application"
4. Configure the settings:
   - **Name**: File Manager OAuth Client
   - **Authorized JavaScript origins**: 
     - `http://localhost:8080` (for development)
     - `https://yourdomain.com` (for production)
   - **Authorized redirect URIs**:
     - `http://localhost:8080/login/oauth2/code/google` (for development)
     - `https://yourdomain.com/login/oauth2/code/google` (for production)
5. Click "Create"
6. Copy the **Client ID** and **Client Secret**

## Step 5: Configure Your Application

### Option A: Using application.properties

Update `src/main/resources/application.properties`:

```properties
# Replace with your actual values
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
```

### Option B: Using Environment Variables

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and add your credentials:
   ```bash
   GOOGLE_CLIENT_ID=your_actual_client_id_here
   GOOGLE_CLIENT_SECRET=your_actual_client_secret_here
   ```

3. Update `application.properties` to use environment variables:
   ```properties
   spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
   spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
   ```

## Step 6: Test the Setup

1. Start your application:
   ```bash
   ./gradlew bootRun
   ```

2. Open your browser and go to `http://localhost:8080`

3. You should be redirected to the login page

4. Click "Continue with Google"

5. Complete the Google OAuth flow

6. You should be redirected back to the file manager with your user info displayed

## Troubleshooting

### Common Issues and Solutions:

#### 1. **"redirect_uri_mismatch" error**
**Problem**: Google OAuth2 redirect URI doesn't match configured URI.
**Solution**:
- Check that your redirect URI in Google Console exactly matches: `http://localhost:8080/login/oauth2/code/google`
- Make sure there are no trailing slashes or extra characters
- If running on different port (e.g., 8081), update both Google Console and application.properties

#### 2. **"invalid_client" error**
**Problem**: OAuth2 client credentials are incorrect or not found.
**Solutions**:
- **Method A**: Verify your Client ID and Client Secret are correct in `application.properties`
- **Method B**: If using environment variables, ensure they're properly set:
  ```bash
  # PowerShell
  $env:GOOGLE_CLIENT_ID="your_client_id"
  $env:GOOGLE_CLIENT_SECRET="your_client_secret"
  ```
- **Method C**: Use fallback values in application.properties:
  ```properties
  spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID:your_actual_client_id}
  spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:your_actual_client_secret}
  ```

#### 3. **"Could not resolve placeholder" error**
**Problem**: Spring Boot can't resolve environment variable placeholders.
**Solution**: Add default values to prevent placeholder resolution errors:
```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID:default_value}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:default_value}
```

#### 4. **"Missing attribute 'id' in attributes" error**
**Problem**: Google OAuth2 provides `sub` attribute but Spring Security expects `id`.
**Solution**: Update the user-name-attribute in application.properties:
```properties
spring.security.oauth2.client.provider.google.user-name-attribute=sub
```

#### 5. **500 Internal Server Error after OAuth2 login**
**Problem**: Authentication succeeds but application fails to process user data.
**Root Cause**: Usually the "Missing attribute 'id'" error (see #4 above).
**Solution**: Fix the user-name-attribute configuration and restart the application.

#### 6. **Port conflicts (Address already in use)**
**Problem**: Port 8080 is already in use by another process.
**Solutions**:
- **Method A**: Run on different port: `./gradlew bootRun --args='--server.port=8081'`
- **Method B**: Kill existing process and restart
- **Method C**: Update Google Console redirect URI to match new port

#### 7. **"access_denied" error**
**Problem**: User not authorized to access the application.
**Solution**:
- Make sure your email is added as a test user in the OAuth consent screen
- Check that the required scopes are configured: `openid`, `profile`, `email`

#### 8. **Application not redirecting after login**
**Problem**: OAuth2 flow completes but user isn't redirected to main application.
**Solution**:
- Check the application logs for detailed error messages
- Verify the SecurityConfig authentication success handler is properly configured
- Ensure the home page controller (`/`) is accessible to authenticated users

### Development vs Production

- **Development**: Use `http://localhost:8080`
- **Production**: Use your actual domain with HTTPS (e.g., `https://yourdomain.com`)

### Security Notes

- Never commit your Client Secret to version control
- Use environment variables or secure configuration management in production
- Consider using Google Cloud Secret Manager for production deployments
- Regularly rotate your OAuth credentials

## Next Steps

Once authentication is working:

1. Consider adding user-specific file storage (each user sees only their files)
2. Implement role-based access control
3. Add user profile management
4. Set up proper session management
5. Configure HTTPS for production

## Support

If you encounter issues:

1. Check the application logs for detailed error messages
2. Verify your Google Cloud Console configuration
3. Test with a simple OAuth2 flow first
4. Consult the [Spring Security OAuth2 documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html)
