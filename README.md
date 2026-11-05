<<<<<<< HEAD
# Banking-Project
=======
# Retail Banking POC — Local Windows Setup (No Docker)

This package runs the seven Spring Boot applications directly from IntelliJ or Maven.
**Docker and Docker Desktop are not required.**

## Components

| Component | Port | Purpose |
|---|---:|---|
| API Gateway | 8080 | Single API entry point |
| Eureka Service Discovery | 8761 | Service registry |
| Identity Service | 8081 | Registration, login, JWT issuance |
| Customer Service | 8082 | Customer profile and KYC |
| Account Service | 8083 | Accounts, deposits, withdrawals, transfers, ledger |
| Loan Service | 8084 | Loan applications, disbursement, repayments |
| Audit Service | 8085 | Audit event consumer and records |
| PostgreSQL | 5432 | One database per service |
| Kafka | 9092 | Asynchronous events |
| Redis | 6379 | Distributed lock support |

## Required software

1. JDK 17
2. PostgreSQL 16 (Windows installer)
3. Apache Kafka (download the binary distribution; run it in KRaft mode)
4. Redis-compatible server on localhost:6379:
   - Memurai for native Windows, or
   - Redis inside WSL2.
5. IntelliJ IDEA
6. Postman (optional, for API testing)
7. Maven 3.9+ (or use IntelliJ's bundled Maven)

Eureka is already a Spring Boot application in this project; do not install it separately.
Zookeeper is not needed when Kafka is run in KRaft mode.

## 1. Configure PostgreSQL

During PostgreSQL installation, remember the password chosen for user `postgres`.
Open pgAdmin Query Tool or `psql` and run `database/create-databases.sql`.

PowerShell example (adjust the path if PostgreSQL was installed elsewhere):

```powershell
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -f ".\database\create-databases.sql"
```

The script creates these databases:
- identity_db
- customer_db
- account_db
- loan_db
- audit_db

If you prefer, create them manually in pgAdmin. If a database already exists, do not create it again.

## 2. Configure database credentials

Each service's `src/main/resources/application.yml` has:

```yaml
password: ${DB_PASSWORD:CHANGE_ME}
```

Replace `CHANGE_ME` with the password you chose for PostgreSQL, consistently in all five services.
Alternatively, define the `DB_PASSWORD` environment variable in each IntelliJ Run Configuration.
Do not commit real credentials to Git.

The default JDBC URLs point to `localhost:5432` and the correct service-specific database.

## 3. Start Kafka (KRaft mode)

Download and extract the Kafka binary distribution from https://kafka.apache.org/downloads.
Use the `config/kraft/server.properties` file included in that Kafka distribution.

Open PowerShell in the Kafka extracted directory:

```powershell
.\bin\windows\kafka-storage.bat random-uuid
```

Copy the generated UUID, then format the storage (replace `<CLUSTER_ID>`):

```powershell
.\bin\windows\kafka-storage.bat format -t <CLUSTER_ID> -c .\config\kraft\server.properties
```

Start the broker:

```powershell
.\bin\windows\kafka-server-start.bat .\config\kraft\server.properties
```

Keep this terminal open. Confirm the broker is listening on `localhost:9092`.
If you have already formatted this Kafka data directory, do not format it again.

## 4. Start Redis

Start Memurai or your Redis server and confirm it listens on `localhost:6379`.
For Memurai, use its Windows service / Memurai command-line tools.
For Redis in WSL2, ensure the Windows host can reach the configured Redis port.

## 5. Build the seven applications

Open PowerShell in this project root:

```powershell
mvn -f service-discovery/pom.xml clean package
mvn -f identity-service/pom.xml clean package
mvn -f customer-service/pom.xml clean package
mvn -f account-service/pom.xml clean package
mvn -f loan-service/pom.xml clean package
mvn -f audit-service/pom.xml clean package
mvn -f api-gateway/pom.xml clean package
```

Or open the seven folders as Maven projects in IntelliJ and build each one.

## 6. Run applications (keep each running)

Start them in this order, each from its own IntelliJ Run Configuration or terminal:

1. `ServiceDiscoveryApplication` — port 8761
2. `IdentityServiceApplication` — port 8081
3. `CustomerServiceApplication` — port 8082
4. `AccountServiceApplication` — port 8083
5. `LoanServiceApplication` — port 8084
6. `AuditServiceApplication` — port 8085
7. `ApiGatewayApplication` — port 8080

Open http://localhost:8761 to verify Eureka. Registered services should appear there.

## 7. API smoke test

Gateway base URL: `http://localhost:8080`

Register:
`POST /api/v1/auth/register`

```json
{"username":"demo","password":"Demo@12345","role":"CUSTOMER"}
```

Login:
`POST /api/v1/auth/login`

```json
{"username":"demo","password":"Demo@12345"}
```

Use the returned token as `Authorization: Bearer <token>` where required.

Create customer:
`POST /api/v1/customers`

```json
{"firstName":"Aarav","lastName":"Sharma","dateOfBirth":"1995-04-18","email":"aarav@example.com","phoneNumber":"+919876543210","address":"Pune"}
```

Create account:
`POST /api/v1/accounts`

```json
{"customerId":1,"accountType":"SAVINGS","initialDeposit":10000.00}
```

Transfer:
`POST /api/v1/accounts/transfers`
Header: `Idempotency-Key: transfer-demo-001`

```json
{"sourceAccountId":1,"targetAccountId":2,"amount":500.00,"description":"Rent"}
```

Loan application:
`POST /api/v1/loans`

```json
{"customerId":1,"accountId":1,"principal":50000.00,"annualInterestRate":12.0,"termMonths":12,"monthlyIncome":60000.00,"creditScore":720}
```

A Postman collection is available at `postman/Retail-Banking-POC.postman_collection.json`.

## Locking behavior included in this POC

- Account transfers use PostgreSQL row-level locks (`SELECT ... FOR UPDATE`) in a single database transaction. Both account rows are locked in ascending ID order to reduce deadlock risk; debit, credit and ledger writes commit or roll back together.
- Account balance updates also use a JPA `@Version` field for optimistic concurrency detection.
- Loan disbursement demonstrates a Redis/Redisson distributed lock. Redis must be running for this flow.
- Transfer requests require an idempotency key. Reusing a key with a different request is rejected.

## Important limitations

This is an educational proof of concept, not a production banking platform.
- Identity issues JWTs, but full JWT validation/authorization enforcement at every gateway/downstream boundary needs hardening.
- Loan disbursement currently demonstrates locking and loan status flow; it is not a fully coordinated cross-service atomic account-credit Saga.
- Kafka event publishing is not a transactional outbox.
- KYC, MFA, encryption/key management, rate limiting, fraud controls, regulatory controls, and production-grade retry/DLQ handling are not complete.
- Use synthetic data only; never use real customer data or real funds.

## Troubleshooting

- `database "..." does not exist`: run the SQL script in `database/create-databases.sql` against the same PostgreSQL instance referenced by `localhost:5432`.
- `password authentication failed`: ensure the password in each service's `application.yml` or `DB_PASSWORD` environment variable matches the PostgreSQL `postgres` user's password.
- `Connection refused` on 9092: start Kafka and verify its advertised listener is `localhost:9092`.
- Redis connection errors: start Memurai/Redis and verify `localhost:6379`.
- Services not visible in Eureka: start Service Discovery first and verify `http://localhost:8761`.
>>>>>>> 21aeef6 (Initial commit - Retail Banking Microservices Project)
