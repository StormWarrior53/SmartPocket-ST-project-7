# SmartPocket - Financial Literacy Platform for Children

SmartPocket е интерактивна финансова платформа, предназначена за деца от 7 до 18 годишна възраст, която цели да развие финансова грамотност чрез геймификация, курсове и финансови симулации.

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Security](#security)
- [Deployment](#deployment)

## Overview

SmartPocket is a full-stack web application designed to teach children (ages 7-24) financial literacy through gamification, interactive learning, and parental oversight. The platform combines educational content with a virtual economy, allowing children to earn pocket money by completing quizzes and games, purchase virtual items from a store, and compete on a global leaderboard.

### Key Concepts

- **Dual User System**: Parents create and manage child accounts, control allowances, and monitor progress
- **Educational Content**: Structured learning paths covering budgeting, saving, and investing
- **Gamification**: XP system, leaderboards, quizzes, and interactive budget games
- **Virtual Economy**: Dual currency system (pocket money earned through activities, allowance set by parents)
- **Pattern Authentication**: Child-friendly 3×3 grid pattern login (similar to Android unlock patterns)

## Tech Stack

### Backend (Server)

- **Framework**: Spring Boot 4.0.0
- **Language**: Java 17
- **Database**: PostgreSQL with JPA/Hibernate
- **Authentication**: JWT (JSON Web Tokens) with JJWT library
- **Security**: Spring Security with BCrypt password hashing
- **Rate Limiting**: Bucket4j (token bucket algorithm - 5 requests/min per IP)
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger UI)
- **Validation**: Jakarta Bean Validation with custom validators
- **DTO Mapping**: MapStruct
- **Utilities**: Lombok, Apache Commons Lang3
- **Environment Management**: java-dotenv for .env file support
- **Build Tool**: Maven with wrapper included

### Frontend (Client)

- **Framework**: React 19
- **Build Tool**: Vite 7
- **Styling**: Tailwind CSS 4
- **Routing**: React Router 7
- **State Management**: React Context API (UserContext for authentication)
- **HTTP Client**: Fetch API with custom authentication wrapper
- **Language**: JavaScript (ES modules)

## Features

### 1. User Management

#### Parent Features

- **Account Registration**: Email-based registration with password validation
- **Child Profile Management**: Create, update, and delete child accounts
- **Allowance Control**: Add money to child's allowance for budget games
- **Inventory Monitoring**: View items purchased by children
- **Pattern Reset**: Reset child's login pattern (invalidates all child sessions for 24 hours)
- **Multi-Child Support**: Manage multiple children under one parent account

#### Child Features

- **Pattern-Based Authentication**: 3×3 grid pattern login (minimum 4 unique points, 1-9)
  - First-time pattern setup flow
  - BCrypt-hashed pattern storage
  - Parent can reset if forgotten
- **Profile Dashboard**: View XP, pocket money, allowance, and inventory
- **Auto-Logout**: Automatic session termination on token expiration with 5-minute warning

### 2. Financial Education System

#### Learning Paths (Roadmap)

- **Structured Curriculum**: Two main paths - Budgeting and Investing
- **Introductory Module**: Foundation concepts before path selection
- **Difficulty Levels**: BEGINNER, INTERMEDIATE, ADVANCED
- **Lecture Details**: Title, description, path category, and difficulty
- **Progress Tracking**: Children advance through courses at their own pace

#### Quizzes

- **Lecture-Based Assessments**: Quizzes tied to specific lectures
- **Configurable Pass Percentage**: Set minimum score to pass
- **Pocket Money Rewards**: Children earn money for passing quizzes
- **XP Awards**: Experience points granted for quiz completion
- **Immediate Feedback**: Pass/fail results with score display

### 3. Gamification System

#### Experience Points (XP)

- Earned through quiz completion and game success
- Tracked per child for leaderboard ranking
- Incremental updates via PATCH /api/children/me/xp

#### Leaderboard

- **Public Endpoint**: No authentication required (GET /api/children/leaderboard)
- **Top 100 Display**: Shows children ranked by XP (descending)
- **Current User Highlight**: User's position highlighted in leaderboard
- **Social Competition**: Encourages engagement and progress

#### Interactive Budget Game

- **Monthly Budget Planning**: Allocate allowance across 6 expense categories
  - Food (minimum: 15%)
  - Utilities (minimum: 10% with subcategories: electricity, water, internet)
  - Transportation (minimum: 5%)
  - Clothing (minimum: 5%)
  - Entertainment (minimum: 5%)
  - Miscellaneous
- **Savings Requirement**: Must save at least 20% to win
- **Random Events System**:
  - 40% chance of unexpected expense (e.g., "Your bike broke")
  - 30% chance of gift/bonus (e.g., "Grandma sent money")
  - 30% chance of no event
- **Real-Time Calculations**: Dynamic budget tracking with visual progress bars
- **Rewards**: Successful completion awards pocket money and XP
- **Visual Feedback**: Win/lose modals with themed character images

### 4. Virtual Economy

#### Dual Currency System

1. **Pocket Money**:
   - Earned by completing quizzes and games
   - Used to purchase items from the store
   - Cannot be set by parents (earned only)
   - Updated via PATCH /api/children/me/pocket

2. **Allowance Money**:
   - Set by parents via POST /api/parents/me/children/{id}/add-money
   - Used as budget in the Budget Game
   - Separate from earned money
   - Updated via PATCH /api/children/me/allowance

#### Store System

- **Virtual Item Shop**: Browse and purchase items with pocket money
- **Item Attributes**: Name, emoji, description, price, stock
- **Price Filtering**: View items above/below specific prices
- **Sorting**: Ascending/descending price ordering
- **Stock Management**: Prevents overselling, restocking via admin endpoint
- **Purchase Validation**: Checks sufficient funds and stock before transaction

#### Inventory System

- **Purchase History**: Tracks all items bought by child
- **Historical Pricing**: Records price paid at time of purchase
- **Quantity Tracking**: Supports multiple quantities of same item
- **Unique Constraint**: One inventory record per child-item pair
- **Parent Visibility**: Parents can view child's inventory
- **Removal**: Children can delete items from inventory

### 5. Security Features

#### Authentication & Authorization

- **JWT-Based Authentication**: Stateless token system with 24-hour expiration
- **Role-Based Access**: Separate tokens for "parent" and "child" roles
- **Token Blacklisting**: In-memory blacklist for logout and pattern reset
  - Individual token blacklisting on logout
  - Bulk user token invalidation on pattern reset
  - Auto-cleanup of expired tokens
- **Password Security**: BCrypt hashing (strength 10) for passwords and patterns
- **Ownership Validation**: Enforces parent-child relationships in API calls
- **Access Control**: 403 Forbidden on unauthorized resource access

#### Rate Limiting (RateLimitFilter)

- **Token Bucket Algorithm**: 5 requests per minute per IP address
- **Failed Login Protection**: 10 failed attempts → 5-minute IP block
- **Targeted Endpoints**: Login, register, pattern setup, check-name
- **IP Extraction**: Supports X-Forwarded-For and X-Real-IP headers
- **Brute Force Prevention**: Protects against credential stuffing attacks

#### Input Validation

- **Bean Validation**: @NotBlank, @Email, @Size, @Min, @Max annotations
- **Custom Validators**: @ValidPattern for pattern authentication
- **Pattern Rules**: 4+ unique points, range 1-9, no duplicates
- **Age Validation**: Children must be 7-24 years old
- **Unique Constraints**: Email uniqueness, (parent, child name) uniqueness

#### CORS Configuration

- **Allowed Origins**: Configurable via CORS_ALLOWED_ORIGINS environment variable
- **Default**: <http://localhost:5173>, <http://localhost:3000>
- **Allowed Methods**: GET, POST, PUT, PATCH, DELETE, OPTIONS
- **Credentials**: Enabled for cookie/header support

### 6. Logging & Monitoring

#### Multi-Layer Logging System

1. **MdcFilter** (@Order(1)): Adds request correlation ID, IP, URI, user to MDC
2. **LoggingFilter** (@Order(2)): Logs all HTTP requests/responses with duration
3. **AuthenticationLoggingFilter** (@Order(3)): Logs authentication attempts
4. **Access Denied Logger**: Logs 403 Forbidden attempts
5. **Authentication Entry Point Logger**: Logs 401 Unauthorized attempts

#### Log Levels

- **INFO**: Business events (logins, purchases, CRUD operations)
- **DEBUG**: Detailed flow (service method entry/exit, SQL queries in dev mode)
- **WARN**: Security events (blacklisted tokens, rate limit exceeded)
- **ERROR**: Exceptions with full stack traces

#### Log Output

- **Console**: Standard output for development
- **File**: logs/app.log for persistent storage
- **Format**: Includes MDC fields (requestId, ip, uri, user)

## Architecture

### Backend Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT (React App)                      │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP/JSON
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   FILTER CHAIN                              │
│  RateLimitFilter → JwtAuthenticationFilter                  │
└────────────────────────┬────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   CONTROLLERS                               │
│  ParentController │ ChildController │ ExerciseController    │
│  StoreItemController │ InventoryController                  │
└────────────────────────┬────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    SERVICES                                 │
│  ParentService │ ChildService │ ExerciseService             │
│  StoreItemService │ InventoryService │ TokenBlacklistService│
└────────────────────────┬────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  REPOSITORIES (JPA)                         │
│  ParentRepository │ ChildRepository │ ExerciseRepository    │
│  StoreItemRepository │ InventoryItemRepository             │
└────────────────────────┬────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              DATABASE (PostgreSQL)                          │
│  Parent │ Child │ Exercise │ StoreItem │ InventoryItem      │
└─────────────────────────────────────────────────────────────┘
```

### Frontend Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     ROUTING (React Router)                  │
│  Home │ Login │ Profile │ Roadmap │ Store │ Games          │
└────────────────────────┬────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              STATE MANAGEMENT (Context API)                 │
│  UserContext (auth state, login/logout, token expiration)   │
└────────────────────────┬────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   COMPONENTS                                │
│  Header │ Footer │ ProfileCard │ LectureCard │ StoreCard   │
└────────────────────────┬────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  API LAYER (api.js)                         │
│  authFetch() │ authHeaders() │ logout callback              │
└────────────────────────┬────────────────────────────────────┘
                         ▼
                   BACKEND REST API
```

## Getting Started

### Prerequisites

**Backend:**

- Java 17 or higher
- PostgreSQL 13+
- Maven 3.6+ (or use included Maven wrapper)

**Frontend:**

- Node.js 18+
- npm 9+ (comes with Node.js)

### Backend Setup

1. **Navigate to server directory:**

   ```bash
   cd server
   ```

2. **Create PostgreSQL database:**

   ```sql
   CREATE DATABASE smartpocket;
   ```

3. **Copy environment template:**

   ```bash
   cp .env.example .env
   ```

4. **Configure `.env` file:**

   ```env
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=smartpocket
   DB_USERNAME=your_postgres_username
   DB_PASSWORD=your_postgres_password

   SERVER_PORT=8080

   JWT_SECRET=your_256_bit_secret_key_here_change_in_production
   JWT_EXPIRATION=86400000

   CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
   ```

5. **Run the application:**

   ```bash
   # Using Maven wrapper (recommended)
   ./mvnw spring-boot:run

   # On Windows
   mvnw.cmd spring-boot:run

   # Or with installed Maven
   mvn spring-boot:run
   ```

6. **Verify server is running:**
   - API: <http://localhost:8080>
   - Swagger UI: <http://localhost:8080/swagger-ui.html>
   - API Docs: <http://localhost:8080/v3/api-docs>

### Frontend Setup

1. **Navigate to client directory:**

   ```bash
   cd client
   ```

2. **Install dependencies:**

   ```bash
   npm install
   ```

3. **Configure environment (optional):**
   Create `.env` file:

   ```env
   VITE_API_URL=http://localhost:8080/api
   ```

4. **Start development server:**

   ```bash
   npm run dev
   ```

5. **Access application:**
   - Frontend: <http://localhost:5173>

### First-Time Usage

1. **Register a parent account** at <http://localhost:5173/register>
2. **Login as parent** and navigate to profile
3. **Create a child account** using the "Add Child" button
4. **Logout and login as child** at <http://localhost:5173/login> (toggle to child mode)
5. **Set up pattern** when prompted (e.g., 1,2,5,8)
6. **Explore features**: Browse roadmap, take quizzes, play budget game, visit store

### Development Mode

**Backend (with debug logging):**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Frontend (with hot reload):**

```bash
npm run dev
```

### Production Build

**Backend:**

```bash
./mvnw clean package
java -jar target/server-0.0.1-SNAPSHOT.jar
```

**Frontend:**

```bash
npm run build
npm start  # Serves from dist folder
```

## API Documentation

### Authentication Endpoints

#### Parent Authentication

**POST /api/parents/register**

- **Description**: Register new parent account
- **Access**: Public
- **Request Body**:

  ```json
  {
    "email": "parent@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }
  ```

- **Response**: `201 Created`

  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": 1735862400000
  }
  ```

- **Errors**: 409 Conflict (email exists), 400 Bad Request (validation)

**POST /api/parents/login**

- **Description**: Parent login
- **Access**: Public
- **Request Body**:

  ```json
  {
    "email": "parent@example.com",
    "password": "password123"
  }
  ```

- **Response**: `200 OK` (same as register)
- **Errors**: 401 Unauthorized (invalid credentials)

**GET /api/parents/me**

- **Description**: Get authenticated parent profile
- **Access**: Parent only
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `200 OK`

  ```json
  {
    "id": "uuid",
    "email": "parent@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "createdAt": "2025-12-20T10:30:00"
  }
  ```

**POST /api/parents/logout**

- **Description**: Logout parent (blacklists token)
- **Access**: Parent only
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `204 No Content`

#### Child Authentication

**POST /api/children/check-name**

- **Description**: Check if child exists and if pattern is set
- **Access**: Public
- **Request Body**:

  ```json
  {
    "childName": "Alice",
    "parentEmail": "parent@example.com"
  }
  ```

- **Response**: `200 OK`

  ```json
  {
    "exists": true,
    "hasPattern": false,
    "message": "Child found. Please set up a pattern."
  }
  ```

**POST /api/children/setup-pattern**

- **Description**: First-time pattern setup for child
- **Access**: Public
- **Request Body**:

  ```json
  {
    "childName": "Alice",
    "parentEmail": "parent@example.com",
    "pattern": "1,2,5,8"
  }
  ```

- **Validation**: Pattern must be 4+ unique points (1-9)
- **Response**: `200 OK`

  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresAt": 1735862400000,
    "child": {
      "id": "uuid",
      "name": "Alice",
      "age": 10,
      "xp": 0,
      "pocketMoney": 0,
      "allowanceMoney": 0,
      "hasPattern": true,
      "parentId": "uuid"
    }
  }
  ```

- **Errors**: 409 Conflict (pattern already set), 400 Bad Request (invalid pattern)

**POST /api/children/login**

- **Description**: Child login with pattern
- **Access**: Public
- **Request Body**:

  ```json
  {
    "childName": "Alice",
    "parentEmail": "parent@example.com",
    "pattern": "1,2,5,8"
  }
  ```

- **Response**: `200 OK` (same as setup-pattern)
- **Errors**: 401 Unauthorized (invalid pattern), 404 Not Found (child not found)

**GET /api/children/me**

- **Description**: Get authenticated child profile
- **Access**: Child only
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `200 OK` (same child object as login)

**POST /api/children/logout**

- **Description**: Logout child (blacklists token)
- **Access**: Child only
- **Response**: `204 No Content`

### Child Management Endpoints (Parent Only)

**POST /api/parents/me/children**

- **Description**: Create new child profile
- **Access**: Parent only
- **Request Body**:

  ```json
  {
    "name": "Alice",
    "age": 10
  }
  ```

- **Validation**: Age 7-24, unique name per parent
- **Response**: `201 Created` (child object)

**GET /api/parents/me/children**

- **Description**: Get all children for authenticated parent
- **Access**: Parent only
- **Response**: `200 OK` (array of child objects)

**GET /api/parents/me/children/{childId}**

- **Description**: Get specific child details
- **Access**: Parent only (validates ownership)
- **Response**: `200 OK` (child object)
- **Errors**: 403 Forbidden (not your child), 404 Not Found

**PUT /api/parents/me/children/{childId}**

- **Description**: Update child information
- **Access**: Parent only
- **Request Body**:

  ```json
  {
    "name": "Alice Updated",
    "age": 11
  }
  ```

- **Response**: `200 OK` (updated child object)

**DELETE /api/parents/me/children/{childId}**

- **Description**: Delete child profile (cascades to inventory)
- **Access**: Parent only
- **Response**: `204 No Content`

**POST /api/parents/me/children/{childId}/reset-pattern**

- **Description**: Reset child's pattern (blacklists all child tokens for 24h)
- **Access**: Parent only
- **Response**: `200 OK` (child object with hasPattern=false)

**POST /api/parents/me/children/{childId}/add-money**

- **Description**: Add money to child's allowance
- **Access**: Parent only
- **Request Body**:

  ```json
  {
    "amount": 500
  }
  ```

- **Response**: `200 OK` (updated child object)

**GET /api/parents/me/children/{childId}/inventory**

- **Description**: View child's purchased items
- **Access**: Parent only
- **Response**: `200 OK` (array of inventory items)

### Child Update Endpoints (Child Only)

**PATCH /api/children/me/xp**

- **Description**: Add XP points (incremental)
- **Access**: Child only
- **Request Body**:

  ```json
  {
    "xp": 50
  }
  ```

- **Response**: `200 OK` (updated child object)

**PATCH /api/children/me/pocket**

- **Description**: Add pocket money (incremental)
- **Access**: Child only
- **Request Body**:

  ```json
  {
    "amount": 100
  }
  ```

- **Response**: `200 OK` (updated child object)

**PATCH /api/children/me/allowance**

- **Description**: Set allowance money (absolute value)
- **Access**: Child only
- **Request Body**:

  ```json
  {
    "amount": 1000
  }
  ```

- **Response**: `200 OK` (updated child object)

### Leaderboard & Public Child Endpoints

**GET /api/children/leaderboard**

- **Description**: Get public leaderboard sorted by XP (descending)
- **Access**: Public (no auth required)
- **Response**: `200 OK`

  ```json
  [
    { "id": "uuid", "name": "Alice", "age": 10, "xp": 250 },
    { "id": "uuid", "name": "Bob", "age": 12, "xp": 180 }
  ]
  ```

**GET /api/children**

- **Description**: Get all children
- **Access**: Authenticated
- **Response**: `200 OK` (array of ChildListResponse)

**GET /api/children/by-parent/{parentId}**

- **Description**: Get children by parent ID
- **Access**: Authenticated
- **Response**: `200 OK` (array of ChildListResponse)

### Exercise Endpoints (All Public)

**GET /api/exercises**

- **Description**: Get all exercises/lectures
- **Access**: Public
- **Response**: `200 OK`

  ```json
  [
    {
      "id": "uuid",
      "title": "Introduction to Budgeting",
      "path": "budgeting",
      "description": "Learn the basics...",
      "difficultyLevel": "BEGINNER",
      "createdAt": "2025-12-20T10:00:00",
      "updatedAt": "2025-12-20T10:00:00"
    }
  ]
  ```

**GET /api/exercises/{id}**

- **Description**: Get specific exercise details
- **Response**: `200 OK` (single exercise object)

**GET /api/exercises/difficulty/{level}**

- **Description**: Filter exercises by difficulty
- **Path Params**: level = BEGINNER | INTERMEDIATE | ADVANCED
- **Response**: `200 OK` (array of exercises)

**GET /api/exercises/search?keyword={keyword}**

- **Description**: Search exercises by title (case-insensitive)
- **Query Params**: keyword (string)
- **Response**: `200 OK` (array of matching exercises)

**POST /api/exercises**

- **Description**: Create new exercise
- **Access**: Public
- **Request Body**:

  ```json
  {
    "title": "Saving for Goals",
    "path": "saving",
    "description": "Learn how to save...",
    "difficultyLevel": "INTERMEDIATE"
  }
  ```

- **Response**: `201 Created`

**PUT /api/exercises/{id}**

- **Description**: Update exercise (full replacement)
- **Request Body**: Same as POST
- **Response**: `200 OK`

**DELETE /api/exercises/{id}**

- **Description**: Delete exercise
- **Response**: `204 No Content`

### Store Endpoints (All Public)

**GET /api/store**

- **Description**: Get all store items
- **Access**: Public
- **Response**: `200 OK`

  ```json
  [
    {
      "id": "uuid",
      "name": "Gold Star Badge",
      "description": "Show off your achievements",
      "emoji": "⭐",
      "price": 100,
      "stock": 50
    }
  ]
  ```

**GET /api/store/{id}**

- **Description**: Get specific store item
- **Response**: `200 OK` (single store item)

**GET /api/store/asc**

- **Description**: Get items sorted by price (ascending)
- **Response**: `200 OK` (array of items)

**GET /api/store/desc**

- **Description**: Get items sorted by price (descending)
- **Response**: `200 OK` (array of items)

**GET /api/store/below/{price}**

- **Description**: Get items below certain price
- **Path Params**: price (integer)
- **Response**: `200 OK` (array of items)

**GET /api/store/above/{price}**

- **Description**: Get items above certain price
- **Response**: `200 OK` (array of items)

**POST /api/store**

- **Description**: Create new store item
- **Request Body**:

  ```json
  {
    "name": "Diamond Trophy",
    "description": "Ultimate achievement",
    "emoji": "💎",
    "price": 500,
    "stock": 10
  }
  ```

- **Response**: `201 Created`

**PUT /api/store/{id}**

- **Description**: Full update of store item
- **Request Body**: Same as POST
- **Response**: `200 OK`

**PATCH /api/store/{id}**

- **Description**: Partial update (only non-null fields)
- **Request Body**: Partial StoreItemRequestDTO
- **Response**: `200 OK`

**DELETE /api/store/{id}**

- **Description**: Delete store item (cascades to inventory)
- **Response**: `204 No Content`

**POST /api/store/{id}/restock?amount={amount}**

- **Description**: Add stock to item
- **Query Params**: amount (integer)
- **Response**: `200 OK` (updated item)

### Inventory Endpoints (Child Only)

**GET /api/children/me/inventory**

- **Description**: Get authenticated child's inventory
- **Access**: Child only
- **Response**: `200 OK`

  ```json
  [
    {
      "id": "uuid",
      "childId": "uuid",
      "storeItemId": "uuid",
      "storeItemName": "Gold Star Badge",
      "storeItemEmoji": "⭐",
      "storeItemDescription": "Show off your achievements",
      "quantity": 2,
      "pricePaid": 100,
      "purchasedAt": "2025-12-20T15:30:00"
    }
  ]
  ```

**POST /api/children/me/inventory/purchase**

- **Description**: Purchase item from store
- **Access**: Child only
- **Request Body**:

  ```json
  {
    "storeItemId": "uuid",
    "quantity": 1
  }
  ```

- **Validations**:
  - Sufficient stock available
  - Sufficient pocket money (price × quantity)
  - Atomic transaction (updates stock, money, inventory)
- **Response**: `201 Created` (inventory item object)
- **Errors**: 400 Bad Request (insufficient funds/stock)

**DELETE /api/children/me/inventory/{inventoryItemId}**

- **Description**: Remove item from inventory
- **Access**: Child only (validates ownership)
- **Response**: `204 No Content`

### Error Responses

All error responses follow this format:

```json
{
  "timestamp": "2025-12-20T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: Email must be valid",
  "path": "/api/parents/register"
}
```

**Common Status Codes:**

- `400 Bad Request` - Validation errors, insufficient funds/stock
- `401 Unauthorized` - Invalid credentials, missing/invalid token
- `403 Forbidden` - Access denied, ownership validation failure
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate resource (email, pattern already set)
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Unexpected server error

## Database Schema

### Entity Relationship Diagram

```
┌─────────────────────┐
│      Parent         │
├─────────────────────┤
│ id (PK, UUID)       │
│ email (UNIQUE)      │
│ firstName           │
│ lastName            │
│ passwordHash        │
│ createdAt           │
└──────────┬──────────┘
           │ 1
           │
           │ N
┌──────────▼──────────┐
│      Child          │
├─────────────────────┤
│ id (PK, UUID)       │
│ parent_id (FK)      │
│ name                │
│ age (7-24)          │
│ pinHash (nullable)  │
│ xp                  │
│ pocketMoney         │
│ allowanceMoney      │
└──────────┬──────────┘
           │ 1         UNIQUE: (parent_id, name)
           │           INDEX: parent_id
           │ N
┌──────────▼──────────┐
│  InventoryItem      │
├─────────────────────┤
│ id (PK, UUID)       │
│ child_id (FK)       │◄─────┐
│ store_item_id (FK)  │      │
│ quantity (≥1)       │      │
│ pricePaid           │      │
│ purchasedAt         │      │ N
└─────────────────────┘      │
                             │
UNIQUE: (child_id,           │
         store_item_id)      │
INDEX: child_id              │ 1
                    ┌────────┴─────────┐
                    │    StoreItem     │
                    ├──────────────────┤
                    │ id (PK, UUID)    │
                    │ name             │
                    │ description      │
                    │ emoji            │
                    │ price            │
                    │ stock            │
                    └──────────────────┘

┌─────────────────────┐
│     Exercise        │ (Independent)
├─────────────────────┤
│ id (PK, UUID)       │
│ title               │
│ path (category)     │
│ description         │
│ difficultyLevel     │
│ createdAt           │
│ updatedAt           │
└─────────────────────┘
```

### SQL Schema (PostgreSQL)

```sql
-- Parent table
CREATE TABLE parent (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Child table
CREATE TABLE child (
    id UUID PRIMARY KEY,
    parent_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INTEGER NOT NULL CHECK (age BETWEEN 7 AND 24),
    pin_hash VARCHAR(255),
    xp INTEGER DEFAULT 0,
    pocket_money INTEGER DEFAULT 0,
    allowance_money INTEGER DEFAULT 0,
    FOREIGN KEY (parent_id) REFERENCES parent(id) ON DELETE CASCADE,
    UNIQUE (parent_id, name)
);

CREATE INDEX idx_child_parent ON child(parent_id);

-- Exercise table
CREATE TABLE exercise (
    id UUID PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    path VARCHAR(100) NOT NULL,
    description VARCHAR(2000),
    difficulty_level VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Store item table
CREATE TABLE store_item (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    emoji VARCHAR(10),
    price INTEGER NOT NULL,
    stock INTEGER
);

-- Inventory item table
CREATE TABLE inventory_item (
    id UUID PRIMARY KEY,
    child_id UUID NOT NULL,
    store_item_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 1),
    price_paid INTEGER,
    purchased_at TIMESTAMP NOT NULL,
    FOREIGN KEY (child_id) REFERENCES child(id) ON DELETE CASCADE,
    FOREIGN KEY (store_item_id) REFERENCES store_item(id) ON DELETE CASCADE,
    UNIQUE (child_id, store_item_id)
);

CREATE INDEX idx_inventory_child ON inventory_item(child_id);
```

### Cascade Behaviors

- **Parent deletion** → Deletes all children (CASCADE)
- **Child deletion** → Deletes all inventory items (CASCADE)
- **Store item deletion** → Deletes all inventory references (CASCADE) ⚠️

## Security

### Authentication Flow

1. **User submits credentials** (email/password or name/pattern)
2. **Server validates** credentials against BCrypt hash
3. **Server generates JWT** with userId, email, role, expiration
4. **Client stores token** in localStorage
5. **Client sends token** in Authorization header: `Bearer {token}`
6. **JwtAuthenticationFilter** validates token on each request
7. **Token blacklist check** ensures token not revoked
8. **Authentication set** in Spring Security context
9. **Request proceeds** to controller with authenticated user

### Token Structure

```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "role": "parent",
  "iat": 1735862400,
  "exp": 1735948800
}
```

### Rate Limiting

**Configuration:**

- 5 requests per minute per IP (token bucket)
- 10 failed login attempts → 5-minute block
- Applies to: /login, /register, /setup-pattern, /check-name

**Headers on Rate Limit:**

```
HTTP/1.1 429 Too Many Requests
Retry-After: 60
Content-Type: application/json

{
  "message": "Too many requests. Please try again later.",
  "retryAfter": 60
}
```

### Password Security

- **Algorithm**: BCrypt with strength 10
- **Salt**: Auto-generated per password
- **Pattern Storage**: Same as passwords (BCrypt hash of "1,2,5,8")
- **No Plain Text**: Never stored or logged

### Input Validation

**Pattern Validation Example:**

```java
@ValidPattern
String pattern;  // Must be 4+ unique points (1-9)

// Valid: "1,2,5,8"
// Invalid: "1,2,3" (too short)
// Invalid: "1,2,2,8" (duplicate)
// Invalid: "1,2,10,8" (out of range)
```

## Deployment

### Heroku Deployment

The application is configured for Heroku deployment:

**Backend (Procfile):**

```
web: java -jar target/server-0.0.1-SNAPSHOT.jar
```

**System Properties (system.properties):**

```
java.runtime.version=17
```

**Environment Variables:**
Set in Heroku dashboard or CLI:

```bash
heroku config:set DB_HOST=your-postgres-host
heroku config:set DB_NAME=your-db-name
heroku config:set DB_USERNAME=your-db-user
heroku config:set DB_PASSWORD=your-db-password
heroku config:set JWT_SECRET=your-production-secret
heroku config:set CORS_ALLOWED_ORIGINS=https://your-frontend-url.com
```

**Frontend:**

```bash
# Build
npm run build

# Deploy dist folder to static hosting (Vercel, Netlify, etc.)
# Configure VITE_API_URL to production backend URL
```

### Docker Deployment (Example)

**Backend Dockerfile:**

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend Dockerfile:**

```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM node:18-alpine
WORKDIR /app
COPY --from=build /app/dist ./dist
RUN npm install -g serve
EXPOSE 3000
CMD ["serve", "-s", "dist", "-l", "3000"]
```

### Production Checklist

- [ ] Change JWT_SECRET to strong random value (256+ bits)
- [ ] Configure production database (managed PostgreSQL)
- [ ] Set CORS_ALLOWED_ORIGINS to frontend domain
- [ ] Enable HTTPS (TLS/SSL certificates)
- [ ] Configure proper logging (centralized log aggregation)
- [ ] Set up database backups
- [ ] Configure rate limiting for production load
- [ ] Review and harden security settings
- [ ] Set up monitoring and alerting
- [ ] Configure CDN for frontend assets
- [ ] Enable database connection pooling
- [ ] Review and optimize SQL queries
- [ ] Set up CI/CD pipeline
- [ ] Configure error tracking (Sentry, etc.)

## License

This project is licensed under the MIT License.

---

**Project Status**: Active Development

**Contributors**: ST Project #7 Team

**Last Updated**: December 2025
