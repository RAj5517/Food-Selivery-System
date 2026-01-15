# Postman Testing Guide - Food Delivery System

## 🔴 Why "Rejecting Access" Error?

The error you're seeing:
```
Securing GET /login
Http403ForbiddenEntryPoint : Pre-authenticated entry point called. Rejecting access
```

**Reason:** You're trying to access `/login` in the browser, but:
- We **don't have** a `/login` endpoint (we use `/api/auth/login`)
- Spring Security is blocking it because it's not in our allowed public endpoints
- Our security config only allows: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`

**Solution:** Use Postman to test the **actual API endpoints** we created!

---

## ✅ How to Test with Postman

### **Step 1: Setup Postman**

1. **Download Postman** (if not installed): https://www.postman.com/downloads/
2. **Open Postman**
3. **Create a new Collection**: "Food Delivery System"

---

### **Step 2: Test Authentication APIs**

#### **1. Register Customer**

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/register/customer`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "customer@test.com",
    "password": "password123",
    "name": "John Doe",
    "phone": "1234567890"
  }
  ```

**Expected Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "customer@test.com",
  "role": "CUSTOMER",
  "expiresIn": 86400000
}
```

**Save the token!** You'll need it for protected endpoints.

---

#### **2. Register Restaurant**

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/register/restaurant`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "restaurant@test.com",
    "password": "password123",
    "name": "Pizza Palace",
    "cuisine": "Italian",
    "address": "123 Main St, Mumbai",
    "lat": 19.0760,
    "longitude": 72.8777,
    "phone": "9876543210"
  }
  ```

**Expected Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 2,
  "email": "restaurant@test.com",
  "role": "RESTAURANT",
  "expiresIn": 86400000
  }
```

---

#### **3. Register Delivery Partner**

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/register/delivery`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "delivery@test.com",
    "password": "password123",
    "name": "Raj Kumar",
    "vehicleType": "BIKE",
    "phone": "5555555555"
  }
  ```

**Vehicle Types:** `BIKE`, `SCOOTER`, `CAR`, `BICYCLE`

---

#### **4. Login**

**Request:**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/login`
- **Headers:**
  ```
  Content-Type: application/json
  ```
- **Body (raw JSON):**
  ```json
  {
    "email": "customer@test.com",
    "password": "password123"
  }
  ```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "customer@test.com",
  "role": "CUSTOMER",
  "expiresIn": 86400000
}
```

---

## 🔐 Testing Protected Endpoints

After login, you'll get a JWT token. Use it to access protected endpoints:

### **Step 1: Copy the Token**
From the login/register response, copy the `token` value.

### **Step 2: Add Authorization Header**
In Postman:
1. Go to **Authorization** tab
2. Select **Type:** `Bearer Token`
3. Paste your token in the **Token** field

**OR** manually add header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### **Step 3: Test Protected Endpoint**

**Example:** Get customer profile (will be available in Phase 5)
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/customer/profile`
- **Headers:**
  ```
  Authorization: Bearer YOUR_TOKEN_HERE
  ```

---

## 📋 Postman Collection Setup

### **Create Environment Variables:**

1. Click **Environments** → **Create Environment**
2. Name: "Food Delivery Local"
3. Add variables:
   - `base_url`: `http://localhost:8080`
   - `token`: (leave empty, will be set after login)
   - `customer_token`: (leave empty)
   - `restaurant_token`: (leave empty)
   - `delivery_token`: (leave empty)

### **Use Variables in Requests:**

Instead of hardcoding URLs, use:
- URL: `{{base_url}}/api/auth/login`
- Token: `{{customer_token}}`

---

## 🧪 Testing Scenarios

### **Scenario 1: Test Without Token (Should Fail)**
1. **Request:** `GET http://localhost:8080/api/customer/profile`
2. **No Authorization header**
3. **Expected:** `401 Unauthorized`

### **Scenario 2: Test With Invalid Token (Should Fail)**
1. **Request:** `GET http://localhost:8080/api/customer/profile`
2. **Authorization:** `Bearer invalid_token_here`
3. **Expected:** `401 Unauthorized`

### **Scenario 3: Test With Valid Token (Should Work)**
1. **Request:** `GET http://localhost:8080/api/customer/profile`
2. **Authorization:** `Bearer VALID_TOKEN_FROM_LOGIN`
3. **Expected:** `200 OK` with customer data

### **Scenario 4: Test Role-Based Access**
1. Login as **CUSTOMER**
2. Try to access `/api/admin/users` (should fail with 403)
3. Login as **ADMIN** (when we create admin)
4. Try again (should work)

---

## 🐛 Common Errors & Solutions

### **Error: 401 Unauthorized**
- **Cause:** Missing or invalid token
- **Solution:** 
  - Make sure you're logged in
  - Copy the token correctly
  - Check token hasn't expired (24 hours default)

### **Error: 403 Forbidden**
- **Cause:** Wrong role trying to access endpoint
- **Solution:** Use correct user role for the endpoint

### **Error: 400 Bad Request**
- **Cause:** Invalid request body or validation failed
- **Solution:** Check JSON format, required fields, email format, etc.

### **Error: 409 Conflict**
- **Cause:** Email already exists
- **Solution:** Use a different email

### **Error: Connection Refused**
- **Cause:** Application not running
- **Solution:** Start the app: `.\mvnw.cmd spring-boot:run`

---

## 📝 Quick Test Checklist

- [ ] Application is running (`http://localhost:8080`)
- [ ] Register customer → Get token
- [ ] Register restaurant → Get token
- [ ] Register delivery → Get token
- [ ] Login with customer → Get token
- [ ] Test protected endpoint with token → Should work
- [ ] Test protected endpoint without token → Should fail (401)
- [ ] Test wrong role access → Should fail (403)

---

## 🎯 Next Steps

After Phase 3, you can test:
- **Phase 4:** Restaurant & Menu APIs
- **Phase 5:** Customer & Cart APIs
- **Phase 6:** Order APIs

Each phase will add more endpoints to test!

---

## 💡 Pro Tips

1. **Save Requests:** Save each request in Postman collection for easy reuse
2. **Use Tests:** Add Postman tests to automatically verify responses
3. **Environment Switching:** Create dev/staging/prod environments
4. **Pre-request Scripts:** Auto-generate tokens or set variables
5. **Collection Runner:** Run all requests in sequence

---

**Happy Testing!** 🚀

