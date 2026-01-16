# Food Delivery System - Project Overview

## Table of Contents
1. [Project Introduction](#project-introduction)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Architecture Overview](#architecture-overview)
5. [Design Patterns](#design-patterns)
6. [Data Flow](#data-flow)
7. [Security Architecture](#security-architecture)
8. [Key Features](#key-features)
9. [Project Planning](#project-planning)
10. [Development Phases](#development-phases)

---

## Project Introduction

The **Food Delivery System** is a comprehensive Spring Boot-based REST API backend for a food delivery platform. It provides complete functionality for managing restaurants, customers, orders, payments, delivery partners, and administrative tasks.

### Core Purpose
- Enable customers to browse restaurants, order food, and track deliveries
- Allow restaurants to manage menus, receive orders, and view analytics
- Enable delivery partners to accept deliveries and track earnings
- Provide administrators with system-wide control and analytics

### Target Users
- **Customers**: Browse restaurants, place orders, manage cart, track orders
- **Restaurants**: Manage menu, view orders, update order status, view analytics
- **Delivery Partners**: View available orders, accept deliveries, update location, mark delivered
- **Administrators**: Manage users, approve restaurants, view all orders, system analytics

---

## Technology Stack

### Backend Framework
- **Spring Boot 3.2.0**: Main application framework
- **Java 17**: Programming language
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access layer
- **Spring Mail**: Email notifications

### Database
- **PostgreSQL**: Primary relational database
- **H2**: In-memory database for testing

### Security & Authentication
- **JWT (JSON Web Tokens)**: Stateless authentication
- **BCrypt**: Password hashing
- **Spring Security**: Security framework

### API Documentation
- **SpringDoc OpenAPI (Swagger)**: API documentation and testing UI

### Caching
- **Caffeine Cache**: In-memory caching for performance

### Payment Gateway
- **Razorpay**: Payment processing integration

### Rate Limiting
- **Bucket4j**: Rate limiting implementation

### Build Tool
- **Maven**: Dependency management and build automation

---

## Project Structure

### Directory Organization

```
Food Delivery System/
├── src/main/java/com/fooddelivery/
│   ├── controller/          # REST API endpoints (22 controllers)
│   ├── service/             # Business logic layer (16 services)
│   ├── repository/          # Data access layer (13 repositories)
│   ├── model/               # JPA entities (13 models)
│   ├── dto/                 # Data Transfer Objects (35+ DTOs)
│   ├── config/              # Configuration classes (6 configs)
│   ├── exception/           # Custom exceptions & handlers (5 files)
│   ├── security/            # Security filters (1 filter)
│   ├── util/                # Utility classes (3 utils)
│   └── FoodDeliveryApplication.java
│
├── src/main/resources/
│   ├── application.properties    # Application configuration
│   └── application-test.properties  # Test configuration
│
├── src/test/                # Test files
├── pom.xml                  # Maven dependencies
├── mvnw.cmd                 # Maven wrapper
├── README.md                # Project documentation
└── PROJECT_PLAN.md          # Development plan
```

### Folder Responsibilities

#### `/controller/` - REST API Layer (22 Controllers)
**Purpose**: Handles HTTP requests and responses, validates input, delegates to services

**Categories**:
- **Authentication**: `AuthController`
- **Public Access**: `PublicRestaurantController`, `PublicMenuController`
- **Customer Features**: `CustomerController`, `CartController`, `OrderController`
- **Restaurant Features**: `RestaurantController`, `MenuController`, `RestaurantOrderController`, `RestaurantAnalyticsController`
- **Delivery Features**: `DeliveryPartnerController`, `DeliveryOrderController`, `DeliveryAnalyticsController`
- **Payment**: `PaymentController`
- **Admin**: `AdminUserController`, `AdminRestaurantController`, `AdminOrderController`, `AdminAnalyticsController`
- **Utilities**: `CategoryController`, `FileUploadController`, `CacheController`

**Responsibilities**:
- Receive HTTP requests
- Validate request data using DTOs
- Call appropriate service methods
- Return HTTP responses
- Handle authentication/authorization at method level

#### `/service/` - Business Logic Layer (16 Services)
**Purpose**: Contains all business logic, orchestrates data operations, implements core functionality

**Categories**:
- **Authentication**: `AuthService`, `CustomUserDetailsService`
- **Customer**: `CustomerService`, `CartService`, `OrderService`
- **Restaurant**: `RestaurantService`, `MenuService`, `CategoryService`
- **Delivery**: `DeliveryPartnerService`
- **Payment**: `PaymentService`
- **Email**: `EmailService`
- **Analytics**: `AdminAnalyticsService`, `RestaurantAnalyticsService`, `DeliveryAnalyticsService`, `AdminService`
- **Utilities**: `RateLimitService`

**Responsibilities**:
- Implement business rules and logic
- Coordinate between multiple repositories
- Perform data transformations
- Handle transactions
- Cache management
- Email notifications
- Analytics calculations

#### `/repository/` - Data Access Layer (13 Repositories)
**Purpose**: Interface with database, provides CRUD operations and custom queries

**Repositories**:
- `UserRepository`, `CustomerRepository`, `CustomerAddressRepository`
- `RestaurantRepository`, `MenuItemRepository`, `CategoryRepository`
- `DeliveryPartnerRepository`
- `CartRepository`, `CartItemRepository`
- `OrderRepository`, `OrderItemRepository`
- `PaymentRepository`
- `ReviewRepository`

**Responsibilities**:
- Provide CRUD operations (inherited from JpaRepository)
- Define custom queries using JPA/HQL
- Handle database-specific operations
- Support pagination and sorting
- Complex aggregation queries for analytics

#### `/model/` - Entity Layer (13 Models)
**Purpose**: JPA entities representing database tables, define data structure and relationships

**Entities**:
1. **User**: Central user entity (email, password, role, isActive)
2. **Customer**: Customer profile (name, wallet balance)
3. **CustomerAddress**: Customer delivery addresses
4. **Restaurant**: Restaurant information (name, cuisine, location, rating, isApproved)
5. **Category**: Food categories
6. **MenuItem**: Restaurant menu items (name, price, availability)
7. **DeliveryPartner**: Delivery partner profile (vehicle type, location, availability)
8. **Cart**: Shopping cart (customer, restaurant)
9. **CartItem**: Items in cart (menu item, quantity, price)
10. **Order**: Order entity (customer, restaurant, delivery partner, status, payment)
11. **OrderItem**: Items in order (menu item, quantity, price)
12. **Payment**: Payment information (order, amount, method, status, transaction ID)
13. **Review**: Customer reviews (order, customer, restaurant, rating, comment)

**Responsibilities**:
- Define database schema
- Establish entity relationships (One-to-One, One-to-Many, Many-to-One)
- Define constraints and validations
- Represent database records as Java objects

#### `/dto/` - Data Transfer Objects (35+ DTOs)
**Purpose**: Transfer data between layers, separate internal models from API contracts

**Categories**:
- **Request DTOs**: `LoginRequest`, `RegisterCustomerRequest`, `PlaceOrderRequest`, etc.
- **Response DTOs**: `AuthResponse`, `OrderResponse`, `RestaurantResponse`, etc.
- **Analytics DTOs**: `AdminAnalyticsOverviewResponse`, `DeliveryAnalyticsResponse`, etc.

**Responsibilities**:
- Define API request/response structure
- Hide internal entity structure
- Provide validation constraints
- Support versioning and evolution

#### `/config/` - Configuration Layer (6 Configs)
**Purpose**: Application-wide configuration, beans, security, caching, API documentation

**Configurations**:
1. **SecurityConfig**: Spring Security configuration, role-based access, JWT filter chain
2. **CacheConfig**: Caffeine cache configuration
3. **OpenApiConfig**: Swagger/OpenAPI documentation configuration
4. **RazorpayConfig**: Payment gateway configuration
5. **WebMvcConfig**: MVC configuration (interceptors, CORS)
6. **RateLimitInterceptor**: Rate limiting configuration

**Responsibilities**:
- Configure security rules and authentication
- Set up caching strategies
- Configure API documentation
- Set up interceptors and filters
- Define CORS policies

#### `/exception/` - Exception Handling (5 Files)
**Purpose**: Custom exceptions and global exception handler

**Exceptions**:
- `ResourceNotFoundException`: Entity not found
- `UnauthorizedException`: Authentication/authorization failure
- `ConflictException`: Resource conflict (e.g., duplicate email)
- `BadRequestException`: Invalid request data
- `GlobalExceptionHandler`: Centralized exception handling

**Responsibilities**:
- Define custom exception types
- Provide meaningful error messages
- Convert exceptions to HTTP responses
- Handle validation errors
- Provide consistent error format

#### `/security/` - Security Layer (1 Filter)
**Purpose**: JWT authentication filter

**Components**:
- `JwtAuthenticationFilter`: Intercepts requests, validates JWT tokens, sets authentication context

**Responsibilities**:
- Extract JWT token from Authorization header
- Validate token signature and expiration
- Load user details and set security context
- Allow requests to proceed or reject unauthorized access

#### `/util/` - Utility Classes (3 Utils)
**Purpose**: Reusable utility functions

**Utilities**:
- `JwtUtil`: JWT token generation, validation, extraction
- `SecurityUtil`: Extract user info from security context
- `FileUploadUtil`: File upload handling, validation, storage

**Responsibilities**:
- Provide reusable helper methods
- Encapsulate common operations
- Support multiple layers (service, controller)

---

## Architecture Overview

### Layered Architecture

The project follows a **3-tier layered architecture**:

```
┌─────────────────────────────────────────────┐
│           CONTROLLER LAYER                   │
│   (REST API Endpoints, Request/Response)     │
├─────────────────────────────────────────────┤
│           SERVICE LAYER                      │
│   (Business Logic, Transactions)             │
├─────────────────────────────────────────────┤
│           REPOSITORY LAYER                   │
│   (Data Access, Database Queries)            │
└─────────────────────────────────────────────┘
                │
                ▼
          DATABASE (PostgreSQL)
```

### Request Flow

```
1. HTTP Request
   ↓
2. Security Filter (JwtAuthenticationFilter)
   - Extract JWT token
   - Validate token
   - Set authentication context
   ↓
3. Rate Limit Interceptor (RateLimitInterceptor)
   - Check rate limits
   - Add rate limit headers
   - Reject if limit exceeded
   ↓
4. Controller
   - Validate request DTO
   - Extract parameters
   - Call service method
   ↓
5. Service
   - Implement business logic
   - Call repositories
   - Handle transactions
   - Transform data
   ↓
6. Repository
   - Execute database queries
   - Return entities
   ↓
7. Service
   - Convert entities to DTOs
   - Return DTOs
   ↓
8. Controller
   - Return HTTP response with DTO
   ↓
9. Client receives response
```

### Data Flow

```
API Request (DTO)
    ↓
Controller (validates DTO)
    ↓
Service (business logic)
    ↓
Repository (database queries)
    ↓
Database (entities)
    ↓
Repository (returns entities)
    ↓
Service (converts to DTOs)
    ↓
Controller (returns DTO response)
    ↓
HTTP Response (JSON)
```

---

## Design Patterns

### 1. **Layered Architecture Pattern**
- Clear separation of concerns
- Controller → Service → Repository layers
- Each layer has specific responsibilities

### 2. **Repository Pattern**
- Abstract database access
- Spring Data JPA provides implementation
- Custom queries when needed

### 3. **DTO Pattern**
- Separate internal entities from API contracts
- Request/Response DTOs
- Versioning support

### 4. **Service Layer Pattern**
- Encapsulate business logic
- Transaction management
- Coordination between repositories

### 5. **Dependency Injection**
- Spring's @Autowired annotation
- Loose coupling
- Easy testing and maintenance

### 6. **Exception Handling Pattern**
- Global exception handler
- Custom exception types
- Consistent error responses

### 7. **Builder Pattern** (JWT tokens, cache configuration)

### 8. **Strategy Pattern** (Different authentication strategies)

---

## Security Architecture

### Authentication Flow

```
1. User Registration/Login
   ↓
2. AuthService validates credentials
   ↓
3. JwtUtil generates JWT token
   ↓
4. Token returned to client
   ↓
5. Client includes token in Authorization header
   ↓
6. JwtAuthenticationFilter intercepts request
   ↓
7. Token validated, user loaded
   ↓
8. Security context set
   ↓
9. Request proceeds with authenticated user
```

### Authorization

- **Role-Based Access Control (RBAC)**:
  - `CUSTOMER`: Customer-specific endpoints
  - `RESTAURANT`: Restaurant management endpoints
  - `DELIVERY`: Delivery partner endpoints
  - `ADMIN`: Administrative endpoints

- **Method-Level Security**: `@PreAuthorize("hasRole('ADMIN')")`

- **Endpoint Protection**: Configured in `SecurityConfig`

### Security Features

1. **JWT Authentication**: Stateless token-based authentication
2. **Password Hashing**: BCrypt password encoding
3. **Role-Based Access**: Different endpoints for different roles
4. **Rate Limiting**: Prevents abuse and DoS attacks
5. **CORS Configuration**: Cross-origin resource sharing
6. **Input Validation**: Jakarta Validation annotations
7. **SQL Injection Prevention**: JPA parameterized queries

---

## Key Features

### 1. **Authentication & Authorization**
- User registration (Customer, Restaurant, Delivery Partner)
- JWT-based login
- Role-based access control
- Token refresh capability

### 2. **Restaurant Management**
- Restaurant registration and approval
- Menu management (CRUD operations)
- Restaurant profile updates
- Restaurant listing with filters (cuisine, city, rating)
- Nearby restaurant search (location-based)

### 3. **Order Management**
- Shopping cart management
- Order placement from cart
- Order status tracking
- Order history
- Order cancellation
- Order assignment to delivery partners

### 4. **Payment Processing**
- Razorpay integration
- Payment order creation
- Payment verification
- Refund processing
- Payment history

### 5. **Delivery Management**
- Available orders listing
- Order acceptance by delivery partners
- Location tracking
- Order delivery status updates
- Earnings tracking

### 6. **Analytics**
- **Admin Analytics**: System-wide overview, revenue, top restaurants/customers, order trends
- **Restaurant Analytics**: Dashboard, popular items, revenue breakdown, peak hours
- **Delivery Analytics**: Earnings, completed deliveries, average delivery time

### 7. **Administration**
- User management (list, activate/deactivate)
- Restaurant approval/rejection
- All orders viewing
- System-wide analytics

### 8. **Supporting Features**
- File upload (images for menu items, profiles)
- Email notifications (registration, order status)
- Caching (restaurant and menu items)
- Rate limiting (public, authenticated, payment APIs)
- API documentation (Swagger UI)


---

## Data Model Relationships

### Core Relationships

```
User (1) ──┬── (1) Customer
           ├── (1) Restaurant
           └── (1) DeliveryPartner

Customer (1) ──┬── (*) CustomerAddress
               ├── (1) Cart
               └── (*) Order

Restaurant (1) ──┬── (*) MenuItem
                 └── (*) Order

Cart (1) ─── (*) CartItem
Order (1) ─── (*) OrderItem

Order (1) ─── (1) Payment
Order (1) ─── (1) Review
```

### Key Relationships Explained

1. **User → Customer/Restaurant/DeliveryPartner**: One user can have one role-specific profile
2. **Customer → Addresses**: One customer can have multiple delivery addresses
3. **Customer → Cart**: One customer has one active shopping cart
4. **Cart → CartItems**: One cart contains multiple items
5. **Restaurant → MenuItems**: One restaurant has many menu items
6. **Order → OrderItems**: One order contains multiple items
7. **Order → Payment**: One order has one payment record
8. **Order → Review**: One order can have one review

---

## API Design Principles

### RESTful Design
- Resource-based URLs (`/api/restaurants`, `/api/orders`)
- HTTP methods (GET, POST, PUT, DELETE)
- Stateless communication
- JSON request/response format

### Pagination
- All list endpoints support pagination
- Parameters: `page` (0-indexed), `size`
- Response includes pagination metadata

### Filtering & Sorting
- Query parameters for filtering
- Sort by field and direction
- Examples: `?cuisine=Italian&sortBy=rating&sortDir=desc`

### Status Codes
- `200 OK`: Successful GET, PUT
- `201 Created`: Successful POST (creation)
- `204 No Content`: Successful DELETE
- `400 Bad Request`: Validation errors
- `401 Unauthorized`: Missing/invalid token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict (e.g., duplicate email)
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server errors

---

## Performance Optimizations

### 1. **Caching**
- Restaurant details cached
- Menu items cached
- Cache eviction on updates
- Caffeine in-memory cache

### 2. **Database Optimization**
- Indexes on frequently queried columns
- Efficient query design
- Pagination to limit result sets
- Lazy loading of relationships

### 3. **Rate Limiting**
- Prevents abuse
- Different limits for different endpoint types
- Protects server resources

### 4. **Query Optimization**
- Custom JPA queries
- Native queries for complex operations
- Aggregation queries for analytics

---

## Testing Strategy

### Test Structure
- Unit tests for services
- Integration tests for controllers
- Repository tests for database operations
- Test configuration with H2 database

### Test Coverage Areas
- Authentication and authorization
- Business logic validation
- Database operations
- Exception handling
- API endpoint testing

---

## Deployment Considerations

### Environment Configuration
- `application.properties` for configuration
- Environment variables for sensitive data
- Separate test configuration

### Database Migrations
- SQL scripts for schema changes
- Example: `database_migration_add_delivered_date.sql`

### Production Readiness
- Error handling and logging
- Security best practices
- API documentation
- Rate limiting
- Caching strategies

---

## Summary

This Food Delivery System is a **production-ready Spring Boot application** implementing:
- Complete REST API for food delivery platform
- Secure authentication and authorization
- Multi-role support (Customer, Restaurant, Delivery, Admin)
- Payment gateway integration
- Analytics and reporting
- Rate limiting and caching
- Comprehensive API documentation

The project demonstrates:
- **Clean architecture** with clear layer separation
- **Best practices** in Spring Boot development
- **Security** implementation with JWT
- **Scalability** with caching and rate limiting
- **Maintainability** with organized structure
- **Documentation** with Swagger/OpenAPI

This is a **full-stack backend solution** ready for integration with frontend applications and mobile apps.

