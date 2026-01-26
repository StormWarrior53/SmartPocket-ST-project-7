# Google OAuth Setup Guide

This guide will help you configure Google OAuth login for the SmartPocket application.

## Prerequisites
- Google Cloud Account
- SmartPocket backend and frontend running locally or deployed

## Important Notes
- **No Google+ API Required:** Google+ API is deprecated. Modern OAuth2 uses Google Identity Services
- **Reference:** [Google Identity OAuth2 Policies](https://developers.google.com/identity/protocols/oauth2/policies)
- **Scopes Used:** `openid`, `email`, `profile` (non-sensitive, no verification needed)

## Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click "Select a project" → "New Project"
3. Enter project name: `SmartPocket` (or your preferred name)
4. Click "Create"

## Step 2: Configure OAuth Consent Screen

**Important:** You must configure the OAuth consent screen before creating credentials.

1. In the Google Cloud Console, navigate to **APIs & Services** → **OAuth consent screen**
2. Choose **External** user type (unless you have a Google Workspace - then choose Internal)
3. Click **Create**
4. Fill in the required information:
   - **App name:** `SmartPocket`
   - **User support email:** Your email address
   - **App logo:** (optional)
   - **Application home page:** `http://localhost:5173` (or your domain)
   - **Authorized domains:** (leave empty for localhost testing)
   - **Developer contact information:** Your email address
5. Click **Save and Continue**
6. **Scopes:** Click **Add or Remove Scopes**
   - The following scopes should be included (they're non-sensitive and don't require verification):
     - `openid`
     - `https://www.googleapis.com/auth/userinfo.email`
     - `https://www.googleapis.com/auth/userinfo.profile`
   - These scopes are automatically added by the `openid,profile,email` configuration
7. Click **Save and Continue**
8. **Test users:** Add email addresses for testing (your Google account emails)
   - While the app is in "Testing" status, only these users can sign in
9. Click **Save and Continue**
10. Review the summary and click **Back to Dashboard**

**Note:** For production, you'll need to publish the app or keep it in testing mode with specific test users.

## Step 3: Create OAuth 2.0 Credentials

1. Navigate to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth client ID**
3. Select **Web application**
4. Configure the OAuth client:
   - Name: `SmartPocket Web App`
   - **Authorized JavaScript origins**:
     - Development: `http://localhost:5173`
     - Development (alternative): `http://localhost:3000`
     - Production: `https://yourdomain.com` (add when deployed)
   - **Authorized redirect URIs**:
     - Development: `http://localhost:8080/oauth2/callback/google`
     - Production: `https://api.yourdomain.com/oauth2/callback/google` (add when deployed)
5. Click **Create**
6. Copy the **Client ID** and **Client Secret** (you'll need these for configuration)

**Important Notes:**
- You don't need to enable any additional Google APIs (Google+ API is deprecated)
- The scopes `openid,profile,email` are non-sensitive and don't require app verification
- While in "Testing" status, only test users you added can sign in
- For production with 100+ users, you'll need to publish the app (or keep test users list updated)

## Step 4: Configure Backend Environment Variables

1. Navigate to the server directory: `cd server`
2. Create or update the `.env` file (use `.env.example` as template):

```bash
# Database Configuration (existing)
DB_HOST=localhost
DB_PORT=5433
DB_NAME=smartpocket
DB_USERNAME=postgres
DB_PASSWORD=your_password_here
SERVER_PORT=8080

# Google OAuth Configuration (NEW)
GOOGLE_CLIENT_ID=your-actual-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-actual-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8080/oauth2/callback/google
FRONTEND_URL=http://localhost:5173
```

Replace `your-actual-client-id` and `your-actual-client-secret` with the values from Step 3.

## Step 5: Configure Frontend Environment Variables

1. Navigate to the client directory: `cd client`
2. Create a `.env` file (use `.env.example` as template):

```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api

# Google OAuth Configuration
VITE_GOOGLE_CLIENT_ID=your-actual-client-id.apps.googleusercontent.com

# Frontend URL
VITE_FRONTEND_URL=http://localhost:5173
```

Replace `your-actual-client-id` with the Client ID from Step 3.

## Step 6: Update Database Schema

The backend will automatically create the new database columns when it starts (using Hibernate `ddl-auto=update`).

**New columns added to `parent` table:**
- `google_id` - Stores Google's unique user ID
- `auth_provider` - Enum: LOCAL or GOOGLE
- `password_hash` - Now nullable (NULL for OAuth users)

## Step 7: Restart the Application

1. **Backend**: Restart the Spring Boot server
   ```bash
   cd server
   mvn spring-boot:run
   ```

2. **Frontend**: Restart the React development server
   ```bash
   cd client
   npm run dev
   ```

## Step 8: Test Google OAuth Login

1. Open the frontend in your browser: `http://localhost:5173`
2. Click **Login**
3. You should see:
   - Email/password login form
   - "OR" divider
   - **Sign in with Google** button
4. Click **Sign in with Google**
5. You will be redirected to Google's authorization page
6. Choose your Google account and authorize the app
7. You should be redirected back to the app and logged in automatically

## Testing Scenarios

### Test 1: New User Registration via Google
- Use a Google account that hasn't registered before
- Expected: New parent account created, automatically logged in

### Test 2: Existing Email/Password User
- First, create a parent account with email/password
- Log out
- Log in with Google using the **same email**
- Expected: Account automatically linked to Google, logged in successfully

### Test 3: Existing Google User
- Log out from a Google-authenticated account
- Log in with Google again
- Expected: Logged in successfully with existing account

### Test 4: OAuth User Trying Password Login
- Create or link a Google account
- Log out
- Try to log in with email/password
- Expected: Error message: "This account uses Google Sign-In. Please use the 'Sign in with Google' button."

## Production Deployment

When deploying to production:

1. **Update Google Cloud Console:**
   - Add production URLs to Authorized JavaScript origins: `https://yourdomain.com`
   - Add production redirect URIs: `https://api.yourdomain.com/oauth2/callback/google`

2. **Update Backend `.env`:**
   ```bash
   GOOGLE_REDIRECT_URI=https://api.yourdomain.com/oauth2/callback/google
   FRONTEND_URL=https://yourdomain.com
   ```

3. **Update Frontend `.env`:**
   ```bash
   VITE_API_BASE_URL=https://api.yourdomain.com/api
   VITE_FRONTEND_URL=https://yourdomain.com
   ```

4. **Publish OAuth Consent Screen** (when ready for production):
   - Go to **APIs & Services** → **OAuth consent screen**
   - While in "Testing" status: Limited to 100 test users
   - To publish to all users: Click **Publish App**
   - **Note:** The scopes we use (`openid`, `email`, `profile`) are non-sensitive and don't require Google verification
   - However, publishing makes the app available to all Google users (not just test users)

## Troubleshooting

### Issue: "redirect_uri_mismatch" error
**Solution:** Make sure the redirect URI in your Google Cloud Console exactly matches the one in your backend configuration.

### Issue: "Email not verified by Google" error
**Solution:** Use a Google account with a verified email address.

### Issue: OAuth callback redirects but doesn't log in
**Solution:** Check browser console for errors. Verify that the frontend callback page is receiving the token in the URL fragment.

### Issue: CORS errors during OAuth flow
**Solution:** Ensure `CORS_ALLOWED_ORIGINS` in backend includes your frontend URL.

### Issue: Database errors about null password_hash
**Solution:** Restart the backend to apply database schema changes (Hibernate will make `password_hash` nullable).

### Issue: "Access blocked: SmartPocket has not completed the Google verification process"
**Solution:** This happens if you try to use an account that's not in the test users list while the app is in "Testing" status. Either:
- Add the Google account to the test users list in OAuth consent screen
- Or publish the app (no verification needed for the scopes we use)

### Issue: User can't sign in - "This app is blocked"
**Solution:** Make sure you've added the user's email to the test users list in the OAuth consent screen (while app is in Testing status).

## Architecture Overview

**OAuth Flow:**
1. User clicks "Sign in with Google" → Frontend redirects to `/oauth2/authorization/google`
2. Backend (Spring Security) redirects to Google authorization page
3. User authorizes → Google redirects to backend callback: `/oauth2/callback/google`
4. Backend processes OAuth, generates JWT token
5. Backend redirects to frontend: `/auth/google/callback#token=xxx&...`
6. Frontend parses token from URL fragment, stores in localStorage, logs user in

**Account Linking:**
- If Google email matches existing parent email → Accounts automatically linked
- Existing children, admin status, and other data preserved
- User can still use password login if they set one before linking (not recommended)

## Security Notes

- JWT tokens expire after 24 hours (configurable)
- State parameter automatically handled by Spring Security (CSRF protection)
- Email verification checked (only verified Google emails accepted)
- OAuth users don't have passwords stored in the database

## Support

For issues or questions:
- Check the console logs (backend and frontend)
- Review the implementation plan: `/Users/stanko/.claude/plans/generic-stirring-forest.md`
- Report issues on GitHub

---

**Last Updated:** January 2026
