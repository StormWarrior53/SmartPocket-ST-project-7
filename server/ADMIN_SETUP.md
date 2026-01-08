# Admin User Setup Guide

## Overview
For security reasons, there is no public API endpoint to register admin users. Admin users must be created manually in the database.

## Production Setup Methods

### Option 1: SQL Script (Recommended for Initial Setup)

Create an admin user directly in the database using SQL:

```sql
-- Replace these values with your admin's information
INSERT INTO parent (id, email, first_name, last_name, password_hash, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin@yourcompany.com',
    'Admin',
    'User',
    '$2a$10$YOUR_BCRYPT_HASH_HERE',  -- See below for generating hash
    NOW(),
    NOW()
);
```

### Generating BCrypt Password Hash

You can generate a BCrypt hash for your password using one of these methods:

**Method 1: Using Online BCrypt Generator**
- Visit: https://bcrypt-generator.com/
- Enter your desired password
- Use rounds: 10
- Copy the generated hash

**Method 2: Using Java (in your project)**
```java
// Run this in a test or main method
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "your-secure-password";
        String hash = encoder.encode(password);
        System.out.println("BCrypt Hash: " + hash);
    }
}
```

**Method 3: Using Spring Boot Shell**
```bash
./mvnw spring-boot:run
# Then in another terminal:
curl -X POST http://localhost:8080/hash-password \
  -H "Content-Type: application/json" \
  -d '{"password": "your-password"}'
```

### Option 2: Database Migration Script

Create a Flyway or Liquibase migration:

**File: `src/main/resources/db/migration/V2__create_admin_user.sql`**
```sql
-- Only run if admin doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM parent WHERE email = 'admin@yourcompany.com') THEN
        INSERT INTO parent (id, email, first_name, last_name, password_hash, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            'admin@yourcompany.com',
            'Admin',
            'User',
            '$2a$10$YOUR_BCRYPT_HASH_HERE',
            NOW(),
            NOW()
        );
    END IF;
END $$;
```

### Option 3: Environment-Based Admin (Advanced)

Create a CommandLineRunner that creates an admin user on first startup:

**File: `src/main/java/org/example/server/config/AdminInitializer.java`**
```java
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {

    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:#{null}}")
    private String adminEmail;

    @Value("${admin.password:#{null}}")
    private String adminPassword;

    @Value("${admin.firstName:Admin}")
    private String adminFirstName;

    @Value("${admin.lastName:User}")
    private String adminLastName;

    @Bean
    CommandLineRunner initAdmin() {
        return args -> {
            if (adminEmail != null && adminPassword != null) {
                if (!parentRepository.existsByEmail(adminEmail)) {
                    Parent admin = Parent.builder()
                        .email(adminEmail)
                        .firstName(adminFirstName)
                        .lastName(adminLastName)
                        .passwordHash(passwordEncoder.encode(adminPassword))
                        .build();

                    parentRepository.save(admin);
                    log.info("Admin user created: {}", adminEmail);
                } else {
                    log.info("Admin user already exists: {}", adminEmail);
                }
            }
        };
    }
}
```

**Then set environment variables:**
```bash
export ADMIN_EMAIL=admin@yourcompany.com
export ADMIN_PASSWORD=your-secure-password
export ADMIN_FIRST_NAME=Admin
export ADMIN_LAST_NAME=User

./mvnw spring-boot:run
```

## Generating Admin JWT Token

Once your admin user is created, they can log in via:

```bash
curl -X POST http://localhost:8080/api/parents/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@yourcompany.com",
    "password": "your-password"
  }'
```

**Important:** You need to modify the `ParentService.login()` method to return role "admin" for admin users. You can do this by:

1. Adding an `isAdmin` boolean field to the Parent model
2. Checking this field in the login method
3. Returning the appropriate role in the JWT

## Security Best Practices

1. **Never commit passwords or hashes to version control**
2. **Use strong, unique passwords for admin accounts**
3. **Store admin credentials in environment variables or secret management systems**
4. **Rotate admin passwords regularly**
5. **Use HTTPS in production**
6. **Consider implementing 2FA for admin accounts**
7. **Limit admin user creation to database-level operations only**

## Checking Admin Access

To verify your admin user works:

```bash
# Login
RESPONSE=$(curl -s -X POST http://localhost:8080/api/parents/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@yourcompany.com","password":"your-password"}')

# Extract token
TOKEN=$(echo $RESPONSE | jq -r '.token')

# Test admin endpoint (create quiz)
curl -X GET http://localhost:8080/api/quizzes \
  -H "Authorization: Bearer $TOKEN"
```

If you get a 403 Forbidden, the user doesn't have admin role in their JWT token.

## Troubleshooting

**Problem:** "Access Denied" when trying to manage quizzes
**Solution:** Ensure the JWT token has `"role": "admin"` claim. Check the `ParentService.login()` method.

**Problem:** Can't login with admin credentials
**Solution:** Verify the password hash is correct and the email matches exactly.

**Problem:** Admin user exists but has PARENT role
**Solution:** You need to distinguish admin users from regular parents. Consider adding an `isAdmin` or `role` column to the parent table.

## Next Steps

After creating your admin user:
1. Login to get JWT token
2. Navigate to `/admin/quiz` in the frontend
3. Start creating quizzes!
