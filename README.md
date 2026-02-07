 # Fin Buzz

Fin Buzz is a microservices-based fintech backend composed of several Spring Boot services. The repository includes service discovery, API gateway, authentication, configuration, and domain services for accounts, transactions, transfers, and notifications.

## Services

- account-service
- admin-service
- authentication-service
- config-service
- fund-transfer
- gateway-service
- notification-service
- registry-service
- transaction-service

## Prerequisites

- Java 17+ (recommended for Spring Boot)
- Maven (or use the included mvnw/mvnw.cmd per service)
- Docker (optional, for containerized runs)

## Quick start (local)

Each service is a standalone Spring Boot application. Start them in this typical order:

1. config-service
2. registry-service
3. authentication-service
4. gateway-service
5. domain services (account, transaction, fund-transfer, notification, admin)

Example (PowerShell):

```powershell
cd .\config-service
.\mvnw.cmd spring-boot:run
```

Repeat for each service in a new terminal window.

## Build

From any service directory:

```powershell
.\mvnw.cmd clean package
```

## Docker

Each service includes a Dockerfile. Example:

```powershell
cd .\account-service
# Build image
 docker build -t finbuzz/account-service:local .
```

## Project structure

```
Fin Buzz/
  account-service/
  admin-service/
  authentication-service/
  config-service/
  fund-transfer/
  gateway-service/
  notification-service/
  registry-service/
  transaction-service/
```

## Notes

- Configuration and service discovery are handled by config-service and registry-service.
- API routing and cross-cutting concerns are handled by gateway-service.

## License

 

