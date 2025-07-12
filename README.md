# CoreFit API Documentation

This document provides a detailed overview of the CoreFit backend APIs, designed for the Flutter mobile app and provider web dashboard. The base URL for the deployed backend is `https://core-fit-production.up.railway.app/`. All endpoints are secured with JWT authentication unless otherwise specified.

## Authentication
- **Token**: Include `Authorization: Bearer <JWT_TOKEN>` in the request header for authenticated endpoints.
- **Base URL**: `https://core-fit-production.up.railway.app/`

## APIs

### 1. Wallet Management

#### 1.1 Get Wallet Balance
- **Endpoint**: `GET /wallet`
- **Description**: Retrieves the current wallet balance for the authenticated user.
- **Request Headers**:
  - `Authorization: Bearer <JWT_TOKEN>`
- **Request Parameters**: None
- **Request Example**:
  ```
  GET https://core-fit-production.up.railway.app/wallet
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  ```
- **Response**:
  - **Status**: 200 OK
  - **Body**:
    ```json
    {
      "status": "success",
      "data": 150.50
    }
    ```
- **Notes**: Returns the wallet balance in EGP.

#### 1.2 Get Wallet Transactions
- **Endpoint**: `GET /wallet/transactions`
- **Description**: Retrieves a paginated list of wallet transactions for the authenticated user.
- **Request Headers**:
  - `Authorization: Bearer <JWT_TOKEN>`
- **Query Parameters**:
  - `page` (optional, default: 1): Page number
  - `size` (optional, default: 10): Items per page
- **Request Example**:
  ```
  GET https://core-fit-production.up.railway.app/wallet/transactions?page=1&size=5
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  ```
- **Response**:
  - **Status**: 200 OK
  - **Body**:
    ```json
    {
      "status": "Wallet transactions fetched successfully",
      "data": {
        "transactions": [
          {
            "id": 1,
            "userId": 1001,
            "userName": "john_doe",
            "type": "DEPOSIT",
            "amount": 50.00,
            "purpose": "Stripe Payment deposit amount 50.0 EGP",
            "timestamp": "2025-07-12T23:00:00Z"
          }
        ],
        "currentPage": 1,
        "totalPages": 2,
        "totalElements": 8,
        "pageSize": 5
      }
    }
    ```
- **Notes**: Transactions are sorted by timestamp in descending order.

#### 1.3 Create Deposit via Stripe
- **Endpoint**: `POST /wallet/deposit`
- **Description**: Initiates a Stripe payment session for wallet deposit.
- **Request Headers**:
  - `Authorization: Bearer <JWT_TOKEN>`
- **Request Body**:
  ```json
  {
    "amount": 100.50
  }
  ```
- **Request Example**:
  ```
  POST https://core-fit-production.up.railway.app/wallet/deposit
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  Content-Type: application/json

  {
    "amount": 100.50
  }
  ```
- **Response**:
  - **Status**: 200 OK
  - **Body**:
    ```json
    {
      "status": "success",
      "data": {
        "stripe_web_view": "https://checkout.stripe.com/pay/cs_test_abc123..."
      }
    }
  ```
- **Notes**: Redirects to Stripe's payment page. Amount is in EGP.

#### 1.4 Handle Stripe Success
- **Endpoint**: `GET /wallet/deposit/success`
- **Description**: Processes a successful Stripe payment and updates the wallet.
- **Request Headers**: None
- **Query Parameters**:
  - `session_id`: Stripe session ID
- **Request Example**:
  ```
  GET https://core-fit-production.up.railway.app/wallet/deposit/success?session_id=cs_test_abc123
  ```
- **Response**:
  - **Status**: 200 OK
  - **Body**:
    ```json
    {
      "status": "Deposit successful"
    }
    ```
- **Notes**: Updates wallet balance and logs the transaction.

#### 1.5 Withdraw from Wallet
- **Endpoint**: `POST /wallet/withdraw`
- **Description**: Withdraws a specified amount from the user's wallet.
- **Request Headers**:
  - `Authorization: Bearer <JWT_TOKEN>`
- **Request Body**:
  ```json
  {
    "amount": 50.00
  }
  ```
- **Request Example**:
  ```
  POST https://core-fit-production.up.railway.app/wallet/withdraw
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  Content-Type: application/json

  {
    "amount": 50.00
  }
  ```
- **Response**:
  - **Status**: 200 OK
  - **Body**:
    ```json
    {
      "status": "Withdrawal successful"
    }
    ```
- **Notes**: Requires sufficient balance; triggers a notification.

### 2. Provider Statistics

#### 2.1 Get Provider Statistics
- **Endpoint**: `GET /provider/stats`
- **Description**: Retrieves statistics for a provider, including orders, reservations, and income for a specific month.
- **Request Headers**:
  - `Authorization: Bearer <JWT_TOKEN>`
- **Query Parameters**:
  - `month` (optional, default: current month): Month number (1-12)
- **Request Example**:
  ```
  GET https://core-fit-production.up.railway.app/provider/stats?month=7
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  ```
- **Response**:
  - **Status**: 200 OK
  - **Body**:
    ```json
    {
      "status": "Success",
      "data": {
        "overview": [
          {
            "title": "Total Orders",
            "count": 15,
            "changeRate": 10
          },
          {
            "title": "Cancelled Orders",
            "count": 2,
            "changeRate": -5
          },
          {
            "title": "Delivered Orders",
            "count": 13,
            "changeRate": 15
          },
          {
            "title": "Total Reservations",
            "count": 8,
            "changeRate": 20
          },
          {
            "title": "Cancelled Reservations",
            "count": 1,
            "changeRate": -10
          },
          {
            "title": "Store Income",
            "count": 1200.50,
            "changeRate": 5
          },
          {
            "title": "Playground Income",
            "count": 800.25,
            "changeRate": 8
          }
        ],
        "topProducts": [
          {
            "id": 101,
            "name": "Soccer Ball",
            "quantitySold": 50
          }
        ],
        "monthlyIncome": [
          {
            "month": "Jul",
            "storeIncome": 1200.50,
            "reservationIncome": 800.25
          }
        ]
      }
    }
    ```
- **Notes**: Requires provider role; compares current month with the previous month.

### 3. Authentication (Assumed from Context)

#### 3.1 Register User
- **Endpoint**: `POST /auth/register`
- **Description**: Registers a new user (general or provider).
- **Request Body**:
  ```json
  {
    "username": "john_doe",
    "email": "john@example.com",
    "password": "securepass123",
    "type": "PROVIDER"
  }
  ```
- **Response**:
  - **Status**: 201 Created
  - **Body**:
    ```json
    {
      "status": "success",
      "data": "User registered successfully"
    }
    ```

#### 3.2 Login
- **Endpoint**: `POST /auth/login`
- **Description**: Authenticates a user and returns a JWT token.
- **Request Body**:
  ```json
  {
    "email": "john@example.com",
    "password": "securepass123"
  }
  ```
- **Response**:
  - **Status**: 200 OK
  - **Body**:
    ```json
    {
      "status": "success",
      "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
      }
    }
    ```

### 4. Additional Endpoints (Inferred from ER Diagram and Context)

#### 4.1 Create Store
- **Endpoint**: `POST /create_store`
- **Description**: Allows a provider to create a new store.
- **Request Body**:
  ```json
  {
    "name": "Sports Haven",
    "description": "Sports equipment store",
    "categoryId": 1
  }
  ```
- **Response**:
  - **Status**: 201 Created
  - **Body**:
    ```json
    {
      "status": "success",
      "data": "Store created successfully"
    }
    ```

#### 4.2 Book Playground Reservation
- **Endpoint**: `POST /reservations/book`
- **Description**: Books a playground slot for the user.
- **Request Body**:
  ```json
  {
    "playgroundId": 100,
    "slots": ["10:00-12:00"],
    "date": "2025-07-13"
  }
  ```
- **Response**:
  - **Status**: 201 Created
  - **Body**:
    ```json
    {
      "status": "success",
      "data": "Reservation booked successfully"
    }
    ```

## Configuration Updates
- **Stripe Secret Key**: Configured as `${STRIPE_SECRET}` in `application.properties`.
- **Production URL**: Set to `${PRODUCTION_DOMAIN}` for redirect URLs.

## ER Diagram Reference
The API design aligns with the ER diagram, supporting entities like `User`, `Market` (Store), `Order`, `Playground`, `Reservation`, and `WalletTransaction`. Relationships include one-to-many (e.g., User to Market) and many-to-one (e.g., Order to Market).

## Notes
- All monetary values are in EGP.
- Endpoints are case-sensitive.
- Ensure proper JWT token inclusion for authenticated requests.
- Refer to the ER diagram for entity relationships and constraints.
