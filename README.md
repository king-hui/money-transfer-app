# Money Transfer Application

A Spring Boot application that handles money transfers between accounts with support for multiple currencies and transaction management.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [API Documentation](#api-documentation)

## Assumptions
 - Currency Exchange Rates are based on Google (except for AUD to USD)
 - If transfer amount is bigger than the account balance, an InsufficientFundsException is thrown.

## Prerequisites

Before you begin, ensure you have the following installed:

- Java JDK 18 or higher
- Maven 3.6.0 or higher
- An IDE of your choice (IntelliJ IDEA recommended)
- Git

## Getting Started

### Clone the Repository
bash git clone [https://github.com/king-hui/money-transfer-app.git) cd moneytransfer

### Build the Application
bash mvn clean install

This command compiles the code, runs the tests, and packages the application.

## Running the Application
You can run the application in several ways:

### Using Maven
bash mvn spring-boot:run

### Using Java

```bash
java -jar target/moneytransfer-0.0.1-SNAPSHOT.jar
```

### Using IDE
1. Open the project in your IDE
2. Navigate to `com.jpmorgan.moneytransfer.MoneytransferApplication`
3. Right-click and select "Run" or use the run button 

The application will start on port 8080 by default. You can access it at [http://localhost:8080](http://localhost:8080)

## Running Tests
Automated tests are based on the test scenario in the document.

To run the automated tests:
``` bash
mvn test
```
To run a specific test class:
``` bash
mvn test -Dtest=MoneytransferApplicationTests
```

## API Documentation
The Money Transfer API provides the following endpoints:
### Find Account
GET /api/accounts/{accountNumber}

### Transfer Money
POST /api/transfers

{
"sourceAccountNumber": "1",
"destinationAccountNumber": "2",
"amount": 40.00,
"currencyCode": "USD"
}