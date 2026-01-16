# Food Delivery System

A comprehensive Spring Boot-based Food Delivery System backend API with complete features from authentication to analytics.

## Features

- ✅ User Management (Customer, Restaurant, Delivery Partner, Admin)
- ✅ JWT Authentication & Authorization
- ✅ Restaurant Management with Menu Items
- ✅ Customer Profile & Address Management
- ✅ Shopping Cart
- ✅ Order Management with State Machine
- ✅ Payment Integration (Razorpay/Stripe)
- ✅ Delivery Partner Module
- ✅ Reviews & Ratings
- ✅ Email Notifications
- ✅ Caching (Caffeine)
- ✅ Rate Limiting
- ✅ Analytics & Reporting APIs
- ✅ Swagger API Documentation
- ✅ File Upload
- ✅ Complex Queries with Pagination & Sorting

## Technology Stack

- **Framework:** Spring Boot 3.2.0
- **Java Version:** 17+
- **Database:** PostgreSQL
- **Security:** Spring Security + JWT
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Caching:** Caffeine
- **Email:** Spring Mail (SMTP)
- **Payment:** Razorpay / Stripe
- **Build Tool:** Maven
- **Validation:** Jakarta Validation

## Prerequisites

- Java 17 or higher
- PostgreSQL 12+
- IDE (IntelliJ IDEA, Eclipse, VS Code) - Optional
- **Note:** Maven Wrapper is included, no need to install Maven separately

## Prerequisites

Before starting, ensure you have the following installed:

- **Java 17 or higher** - [Download](https://www.oracle.com/java/technologies/downloads/#java17)
- **PostgreSQL 12+** - [Download](https://www.postgresql.org/download/)
- **IDE** (Optional) - IntelliJ IDEA, Eclipse, or VS Code
- **Maven Wrapper** (included in project, no separate installation needed)

---

## Complete Setup Guide

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd "Food Delivery System"
```

### Step 2: Database Setup

#### 2.1 Install PostgreSQL

If not already installed, download and install PostgreSQL from [postgresql.org](https://www.postgresql.org/download/)

#### 2.2 Create Database

Open PostgreSQL command line (`psql`) or pgAdmin, then run:

```sql
CREATE DATABASE food_delivery_db;
```

#### 2.3 Verify Database

```sql
\l  -- List all databases (should see food_delivery_db)
\c food_delivery_db  -- Connect to database
```

### Step 3: Configure Application Properties

The application uses `application.properties` file for configuration. Default values are provided, but for production, you should set environment variables.

#### 3.1 Location

Edit: `src/main/resources/application.properties`

#### 3.2 Required Configurations

All configurations have defaults, but these are **required to customize**:

**Database Configuration:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/food_delivery_db
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
```

**JWT Configuration:**
```properties
jwt.secret=${JWT_SECRET:mySecretKey123456789012345678901234567890123456789012345678901234567890}
jwt.expiration=${JWT_EXPIRATION:86400000}  # 24 hours in milliseconds
```

**Email Configuration (SMTP):**
```properties
spring.mail.host=${MAIL_HOST:sandbox.smtp.mailtrap.io}
spring.mail.port=${MAIL_PORT:2525}
spring.mail.username=${MAIL_USERNAME:your-mailtrap-username}
spring.mail.password=${MAIL_PASSWORD:your-mailtrap-password}
mail.from=${MAIL_FROM:noreply@fooddelivery.com}
```

**Payment Gateway (Razorpay):**
```properties
razorpay.key.id=${RAZORPAY_KEY_ID:your-razorpay-key-id}
razorpay.key.secret=${RAZORPAY_KEY_SECRET:your-razorpay-key-secret}
```

**File Upload:**
```properties
file.upload.dir=${FILE_UPLOAD_DIR:uploads/}
```

### Step 4: Set Environment Variables (Optional but Recommended)

You can set environment variables instead of hardcoding values in `application.properties`:

#### Windows (PowerShell)
```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_postgres_password"
$env:JWT_SECRET="your-secret-key-min-64-characters-long-for-security"
$env:MAIL_HOST="sandbox.smtp.mailtrap.io"
$env:MAIL_PORT="2525"
$env:MAIL_USERNAME="your-mailtrap-username"
$env:MAIL_PASSWORD="your-mailtrap-password"
$env:RAZORPAY_KEY_ID="your-razorpay-key-id"
$env:RAZORPAY_KEY_SECRET="your-razorpay-key-secret"
```

#### Windows (Command Prompt)
```cmd
set DB_USERNAME=postgres
set DB_PASSWORD=your_postgres_password
set JWT_SECRET=your-secret-key-min-64-characters-long-for-security
set MAIL_HOST=sandbox.smtp.mailtrap.io
set MAIL_PORT=2525
set MAIL_USERNAME=your-mailtrap-username
set MAIL_PASSWORD=your-mailtrap-password
set RAZORPAY_KEY_ID=your-razorpay-key-id
set RAZORPAY_KEY_SECRET=your-razorpay-key-secret
```

#### Linux/Mac (Bash)
```bash
export DB_USERNAME="postgres"
export DB_PASSWORD="your_postgres_password"
export JWT_SECRET="your-secret-key-min-64-characters-long-for-security"
export MAIL_HOST="sandbox.smtp.mailtrap.io"
export MAIL_PORT="2525"
export MAIL_USERNAME="your-mailtrap-username"
export MAIL_PASSWORD="your-mailtrap-password"
export RAZORPAY_KEY_ID="your-razorpay-key-id"
export RAZORPAY_KEY_SECRET="your-razorpay-key-secret"
```

### Step 5: Get Required Credentials

#### 5.1 Email Credentials (Mailtrap - for Testing)

1. Go to [Mailtrap.io](https://mailtrap.io/)
2. Sign up for free account
3. Navigate to **Email Testing** > **Inboxes** > **My Inbox**
4. Select **SMTP Settings** > **Integrations** > **Spring**
5. Copy:
   - **Host**: `sandbox.smtp.mailtrap.io`
   - **Port**: `2525`
   - **Username**: (from Mailtrap dashboard)
   - **Password**: (from Mailtrap dashboard)

**Note:** Mailtrap is for testing. For production, use Gmail SMTP or SendGrid:
- **Gmail SMTP**: `smtp.gmail.com`, Port `587`, use App Password
- **SendGrid**: Use SendGrid SMTP credentials

#### 5.2 Payment Gateway Credentials (Razorpay)

1. Go to [Razorpay.com](https://razorpay.com/)
2. Sign up for account
3. Navigate to **Settings** > **API Keys**
4. Generate **Test Keys** (for development) or **Live Keys** (for production)
5. Copy:
   - **Key ID**: (e.g., `rzp_test_xxxxxxxxxx`)
   - **Key Secret**: (e.g., `xxxxxxxxxxxxxxxxxxxxxxxxxx`)

**Note:** Test keys work in test mode. Live keys require account verification.

#### 5.3 Generate JWT Secret

Generate a secure random string (minimum 64 characters):

**Option 1: Online Generator**
- Use [random.org](https://www.random.org/strings/) to generate a 64-character string

**Option 2: Command Line**
```bash
# Linux/Mac
openssl rand -base64 64

# PowerShell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(64))
```

### Step 6: Create Upload Directory

Create the upload directory for file storage:

#### Windows (PowerShell/CMD)
```powershell
mkdir uploads
```

#### Linux/Mac
```bash
mkdir -p uploads
```

The application will automatically create this if it doesn't exist, but it's good to create it beforehand.

### Step 7: Build the Project

#### Windows (PowerShell)
```powershell
.\mvnw.cmd clean install
```

#### Windows (Command Prompt)
```cmd
mvnw.cmd clean install
```

#### Linux/Mac
```bash
./mvnw clean install
```

**Expected Output:** `BUILD SUCCESS`

### Step 8: Run the Application

#### Windows (PowerShell)
```powershell
.\mvnw.cmd spring-boot:run
```

#### Windows (Command Prompt)
```cmd
mvnw.cmd spring-boot:run
```

#### Linux/Mac
```bash
./mvnw spring-boot:run
```

#### Using IDE

1. Open project in IntelliJ IDEA/Eclipse/VS Code
2. Locate `FoodDeliveryApplication.java`
3. Right-click > **Run** or press `F5`

**Expected Output:**
```
Started FoodDeliveryApplication in X.XXX seconds
```

### Step 9: Verify Application is Running

Open browser and navigate to:
- **API Base URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs (JSON)**: `http://localhost:8080/v3/api-docs`

If you see the Swagger UI, the application is running successfully!

### Step 10: Test the Application

#### 10.1 Using Swagger UI (Recommended)

1. Open `http://localhost:8080/swagger-ui.html`
2. Test endpoints directly from the browser
3. Click **Authorize** button to add JWT token for authenticated endpoints

#### 10.2 Using Postman

1. Import Swagger JSON from `http://localhost:8080/v3/api-docs`
2. Set base URL: `http://localhost:8080`
3. For protected endpoints, add header:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

#### 10.3 Quick Test (cURL)

```bash
# Test public endpoint
curl http://localhost:8080/api/restaurants

# Test authentication (register customer)
curl -X POST http://localhost:8080/api/auth/register/customer \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User",
    "phone": "1234567890"
  }'
```

---

## Configuration Summary

### All Required Credentials

| Configuration | Environment Variable | Default Value | Where to Get |
|--------------|---------------------|---------------|--------------|
| **Database Username** | `DB_USERNAME` | `postgres` | Your PostgreSQL username |
| **Database Password** | `DB_PASSWORD` | `postgres` | Your PostgreSQL password |
| **JWT Secret** | `JWT_SECRET` | (provided) | Generate 64+ char random string |
| **Mail Host** | `MAIL_HOST` | `sandbox.smtp.mailtrap.io` | Mailtrap/Gmail/SendGrid |
| **Mail Port** | `MAIL_PORT` | `2525` | Mailtrap: 2525, Gmail: 587 |
| **Mail Username** | `MAIL_USERNAME` | (provided for Mailtrap) | From Mailtrap/Gmail dashboard |
| **Mail Password** | `MAIL_PASSWORD` | (provided for Mailtrap) | From Mailtrap/Gmail dashboard |
| **Mail From** | `MAIL_FROM` | `noreply@fooddelivery.com` | Your sender email |
| **Razorpay Key ID** | `RAZORPAY_KEY_ID` | (placeholder) | From Razorpay dashboard |
| **Razorpay Key Secret** | `RAZORPAY_KEY_SECRET` | (placeholder) | From Razorpay dashboard |
| **File Upload Directory** | `FILE_UPLOAD_DIR` | `uploads/` | Local directory path |

### Default Credentials (Development Only)

The application includes default test credentials for quick setup:

- **Mailtrap Test Credentials**: Pre-configured for testing (check `application.properties`)
- **JWT Secret**: Default secret provided (change in production!)
- **Database**: Default to `postgres/postgres` (change in production!)

⚠️ **Important**: Change all default credentials before deploying to production!

---

## Troubleshooting

### Common Issues

#### 1. Database Connection Error
```
Error: Could not connect to database
```
**Solution:**
- Verify PostgreSQL is running
- Check `spring.datasource.url`, `username`, `password` in `application.properties`
- Ensure database `food_delivery_db` exists

#### 2. Port 8080 Already in Use
```
Error: Port 8080 is already in use
```
**Solution:**
- Change port in `application.properties`: `server.port=8081`
- Or stop the process using port 8080

#### 3. JWT Secret Too Short
```
Error: JWT secret must be at least 64 characters
```
**Solution:**
- Generate a longer JWT secret (minimum 64 characters)
- Set `JWT_SECRET` environment variable

#### 4. Email Not Sending
```
Error: Could not send email
```
**Solution:**
- Verify Mailtrap credentials are correct
- Check `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- For Gmail, use App Password (not regular password)

#### 5. File Upload Directory Not Found
```
Error: Could not create upload directory
```
**Solution:**
- Create `uploads/` directory in project root
- Check write permissions on directory

#### 6. Razorpay Payment Error
```
Error: Razorpay authentication failed
```
**Solution:**
- Verify `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` are correct
- Ensure you're using test keys in test mode or live keys in live mode

---

## Additional Resources

- **API Documentation**: See Swagger UI at `http://localhost:8080/swagger-ui.html`
- **Project Overview**: See [PROJECT_OVERVIEW.md](./PROJECT_OVERVIEW.md)
- **Architecture Details**: See [PROJECT_ARCHITECTURE.md](./PROJECT_ARCHITECTURE.md)
- **Phase Details**: See [PROJECT_PHASES.md](./PROJECT_PHASES.md)
- **Development Plan**: See [PROJECT_PLAN.md](./PROJECT_PLAN.md)

## Project Structure

```
src/main/java/com/fooddelivery/
├── controller/      # REST Controllers
├── service/         # Business Logic
├── repository/      # Data Access Layer
├── model/           # Entity Models
├── dto/             # Data Transfer Objects
├── config/          # Configuration Classes
├── exception/       # Custom Exceptions & Handlers
├── util/            # Utility Classes
└── FoodDeliveryApplication.java
```

## API Endpoints

### Authentication
- `POST /api/auth/register/customer` - Customer registration
- `POST /api/auth/register/restaurant` - Restaurant registration
- `POST /api/auth/register/delivery` - Delivery partner registration
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh-token` - Refresh JWT token

### Restaurant
- `GET /api/restaurants` - List restaurants with pagination & filtering
- `GET /api/restaurant/{id}` - Get restaurant by ID
- `POST /api/restaurant/register` - Register restaurant
- `PUT /api/restaurant/profile` - Update restaurant profile

### Menu
- `GET /api/restaurant/{id}/menu` - Get restaurant menu
- `POST /api/restaurant/menu/items` - Add menu item
- `PUT /api/restaurant/menu/items/{id}` - Update menu item
- `DELETE /api/restaurant/menu/items/{id}` - Delete menu item

### Customer
- `GET /api/customer/profile` - Get customer profile
- `PUT /api/customer/profile` - Update profile
- `POST /api/customer/addresses` - Add address
- `GET /api/customer/addresses` - List addresses

### Cart
- `GET /api/cart` - Get current cart
- `POST /api/cart/add` - Add item to cart
- `PUT /api/cart/items/{id}` - Update quantity
- `DELETE /api/cart/items/{id}` - Remove item
- `DELETE /api/cart/clear` - Clear cart

### Orders
- `POST /api/orders/place` - Place order
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders` - List orders with pagination
- `PUT /api/orders/{id}/cancel` - Cancel order

### Payments
- `POST /api/payments/create-order` - Create payment order
- `POST /api/payments/verify` - Verify payment
- `POST /api/payments/refund/{orderId}` - Process refund

### Delivery
- `GET /api/delivery/orders/available` - Get available orders
- `POST /api/delivery/orders/{id}/accept` - Accept order
- `PUT /api/delivery/orders/{id}/update-location` - Update location
- `PUT /api/delivery/orders/{id}/deliver` - Mark delivered

### Reviews
- `POST /api/reviews/submit` - Submit review
- `GET /api/reviews/restaurant/{id}` - Get restaurant reviews

### Analytics
- `GET /api/admin/analytics/overview` - Admin overview
- `GET /api/restaurant/analytics/dashboard` - Restaurant dashboard
- `GET /api/delivery/analytics/earnings` - Delivery earnings

### Admin
- `GET /api/admin/users` - List all users
- `PUT /api/admin/users/{id}/activate` - Activate/deactivate user
- `GET /api/admin/restaurants/pending-approval` - Pending restaurants
- `PUT /api/admin/restaurants/{id}/approve` - Approve restaurant

## Testing

See [HOW_TO_TEST.md](./HOW_TO_TEST.md) for detailed testing guide.

### Quick Test Commands
```powershell
# Run all tests
.\mvnw.cmd clean test

# Run specific test
.\mvnw.cmd test -Dtest=ApplicationStartupTest
```

### Using Postman
1. Start the application: `.\mvnw.cmd spring-boot:run`
2. Set base URL: `http://localhost:8080`
3. For protected endpoints, add JWT token in Authorization header:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

## Development Phases

See [PROJECT_PLAN.md](./PROJECT_PLAN.md) for complete development plan.

## License

This project is for educational purposes.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

