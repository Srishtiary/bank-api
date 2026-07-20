![Build Status](https://github.com/Srishtiary/bank-api/actions/workflows/build.yml/badge.svg)

# Bank API

A secure, RESTful Banking API built with Spring Boot and Java 21, supporting user authentication, account management, and concurrency-safe money transfers.

## Overview

This project simulates the core backend of a banking system. It handles user registration and login with JWT-based authentication, account creation, deposits, withdrawals, and fund transfers between accounts — with a strong focus on data consistency and safe handling of concurrent transactions.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 4.x
- **Security:** Spring Security + JWT (jjwt 0.12.x)
- **Database:** MySQL 8
- **ORM:** Spring Data JPA / Hibernate
- **Build Tool:** Maven
- **Testing:** JUnit 5, Mockito
- **Containerization:** Docker, Docker Compose
- **CI/CD:** GitHub Actions
- **Other:** Lombok, Jakarta Bean Validation

## Key Features

- User registration and login with JWT authentication
- Role-based access control (USER / ADMIN)
- Create bank accounts (Savings / Current)
- Deposit and withdraw funds
- **Concurrency-safe fund transfers** using pessimistic locking to prevent race conditions
- Paginated transaction history
- Admin endpoints to view all users/accounts and freeze/unfreeze accounts
- Global exception handling with meaningful HTTP status codes
- Unit tests covering core business logic
- Dockerized for easy local setup

## Architecture Highlights

### Concurrency-Safe Transfers

The most important design decision in this project is how fund transfers are handled. When money moves between two accounts, the application:

1. Acquires a pessimistic write lock on both accounts involved, using `SELECT ... FOR UPDATE`
2. Locks accounts in a consistent order (based on account number) to prevent deadlocks when two transfers happen between the same pair of accounts in opposite directions at the same time
3. Validates sufficient balance and active account status before moving any money
4. Performs the debit and credit within a single `@Transactional` boundary, so if anything fails, the entire operation rolls back — no partial transfers are possible

This prevents the classic "double-spending" bug where two simultaneous requests could both read the same starting balance and cause incorrect final balances.

### Security

- Passwords are hashed with BCrypt before storage — plain text passwords are never saved or logged
- JWT tokens are stateless — the server does not store session data, making the API easier to scale horizontally
- Role-based `@PreAuthorize` checks protect admin-only endpoints

## Getting Started

### Prerequisites

- Java 21 (JDK)
- Maven (or use the included `mvnw` wrapper)
- MySQL 8 running locally, OR Docker + Docker Compose

### Option 1: Run Locally

1. Clone the repository
   ```bash
   git clone https://github.com/Srishtiary/bank-api.git
   cd bank-api/bank-api
   ```

2. Create a MySQL database
   ```sql
   CREATE DATABASE bankapi;
   ```

3. Update `src/main/resources/application.yml` with your MySQL credentials, or set environment variables:
   ```bash
   export SPRING_DATASOURCE_USERNAME=root
   export SPRING_DATASOURCE_PASSWORD=yourpassword
   ```

4. Run the application
   ```bash
   ./mvnw spring-boot:run
   ```

The API will be available at `http://localhost:8080`.

### Option 2: Run with Docker Compose

```bash
cd bank-api
docker-compose up --build
```

This spins up both the MySQL database and the Spring Boot application together, fully configured.

### Running Tests

```bash
./mvnw test
```

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|--------------|----------------|
| POST | `/api/auth/register` | Register a new user | No |
| POST | `/api/auth/login` | Log in and receive a JWT token | No |

**Register — Request:**
```json
{
  "name": "Test User",
  "email": "test@example.com",
  "password": "password123"
}
```

**Login — Request:**
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

**Login — Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "email": "test@example.com",
  "role": "USER"
}
```

All endpoints below require the header: `Authorization: Bearer <token>`

### Accounts

| Method | Endpoint | Description |
|--------|----------|--------------|
| POST | `/api/accounts` | Create a new account for the logged-in user |
| GET | `/api/accounts/{accountNumber}` | Get account details |

**Create Account — Request:**
```json
{
  "accountType": "SAVINGS"
}
```

### Transactions

| Method | Endpoint | Description |
|--------|----------|--------------|
| POST | `/api/transactions/deposit` | Deposit money into an account |
| POST | `/api/transactions/withdraw` | Withdraw money from an account |
| POST | `/api/transactions/transfer` | Transfer money between two accounts |
| GET | `/api/transactions/{accountNumber}?page=0&size=10` | Get paginated transaction history |

**Deposit / Withdraw — Request:**
```json
{
  "accountNumber": "123456789012",
  "amount": 5000
}
```

**Transfer — Request:**
```json
{
  "fromAccountNumber": "123456789012",
  "toAccountNumber": "987654321098",
  "amount": 1000
}
```

### Admin (requires ADMIN role)

| Method | Endpoint | Description |
|--------|----------|--------------|
| GET | `/api/admin/users` | List all registered users |
| GET | `/api/admin/accounts` | List all accounts |
| PUT | `/api/admin/accounts/{accountNumber}/freeze` | Freeze an account |
| PUT | `/api/admin/accounts/{accountNumber}/unfreeze` | Unfreeze an account |

## Project Structure

```
bank-api/
├── src/main/java/com/bank/bank_api/
│   ├── config/         # Security and password encoder configuration
│   ├── controller/     # REST controllers
│   ├── dto/             # Request/response objects
│   ├── entity/          # JPA entities
│   ├── exception/       # Custom exceptions and global handler
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # JWT utilities and filters
│   ├── service/         # Service interfaces
│   └── service/impl/    # Service implementations
├── src/test/java/       # Unit tests
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Future Improvements

- Add refresh token support
- Add rate limiting on the login endpoint
- Add idempotency keys to the transfer endpoint to safely handle network retries
- Integrate centralized logging (ELK stack) for production observability
- Deploy to a cloud provider (AWS/Render) with Infrastructure as Code (Terraform)

## Author

Built by [Srishtiary](https://github.com/Srishtiary) as a hands-on project to learn Spring Boot, Java 21, secure API design, and DevOps fundamentals.
