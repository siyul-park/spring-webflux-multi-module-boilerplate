![Build Status](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/build.yml/badge.svg)
![Code Quality](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/check.yml/badge.svg)
![K6 Test](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/k6-test.yml/badge.svg)
![Codecov](https://codecov.io/gh/siyual-park/spring-webflux-multi-module-boilerplate/branch/master/graph/badge.svg?token=ICZfrp7K5c)

# 🌟 Spring Webflux Multi-Module Boilerplate

Welcome to the **Spring Webflux Multi-Module Boilerplate**! This powerful template is designed to help you create high-performance, scalable applications with **Spring Webflux** and **R2DBC**. Whether you're building microservices or large-scale applications, this boilerplate provides you with the essential tools to accelerate your development process.

## 🚀 Key Features

### Business Functionality
- **Token Management:** Easily manage user and client tokens for smooth authentication.
- **Scope Management:** Control permissions effectively to enhance security.
- **CRUD Operations:** Seamlessly create, read, update, and delete user and client data.
- **Dynamic Configuration:** Adjust allowed origins effortlessly to accommodate various clients.

### Core Features
- **In-Memory Caching:** Speed up data access with optimized caching strategies.
- **Persistence Management:** Utilize R2DBC for efficient domain object management.
- **Automatic Data Transformation:** Simplify data transformations during reads and writes.
- **Custom Validation:** Set up tailored validation rules to meet your business needs.
- **Asynchronous Event Handling:** Trigger non-blocking events to boost application responsiveness.
- **Flexible Query Capabilities:** Perform advanced filtering and sorting with ease.
- **DTO Transformation:** Effortlessly convert data transfer objects.
- **Patch Updates:** Differentiate between undefined values and null for precise updates.
- **Scalable Authentication:** Integrate additional authentication methods as required.

## 📋 Requirements

Before you get started, ensure you have the following installed:

### Software Requirements
- **Java Development Kit (JDK) 11**: Required to build and run the application.
    - [Install JDK 11](https://openjdk.org/projects/jdk/11/)
- **Redis**: In-memory data structure store used for caching.
    - [Set Up Redis](https://redis.io/)
- **PostgreSQL**: Database for persistent data storage.
    - [Get Started with PostgreSQL](https://www.postgresql.org/)
- **MongoDB**: NoSQL database used for document storage.
    - [Begin with MongoDB](https://www.mongodb.com/)

## ⚙️ Configuration

### Environment Variables

This application is highly configurable via environment variables. Here are the main ones you’ll need:

| **Variable Name**        | **Description**                             | **Example**                            |
|--------------------------|--------------------------------------------|----------------------------------------|
| **PORT**                 | The server port number                     | `8080`                                 |
| **R2DBC_URL**           | Connection URL for the R2DBC database      | `r2dbc:h2:mem://./tmp/`               |
| **R2DBC_USERNAME**      | Username for the R2DBC database             | `username`                             |
| **R2DBC_PASSWORD**      | Password for the R2DBC database             | `password`                             |
| **MONGODB_URI**         | Connection URL for MongoDB                  | `mongodb://localhost:27017/test`      |
| **MONGODB_EMBEDDED_ENABLE** | Enable embedded MongoDB                 | `true`                                 |
| **REDIS_URI**           | Connection URL for Redis                    | `redis://localhost:6379`              |
| **REDIS_EMBEDDED_ENABLE**| Enable embedded Redis                      | `true`                                 |
| **DATA_LOGGING**        | Logging level for data operations           | `INFO`                                 |
| **ACCESS_TOKEN_AGE**    | Expiration time for access tokens           | `3600s`                                |
| **REFRESH_TOKEN_AGE**   | Expiration time for refresh tokens          | `259200s`                              |
| **CLIENT_ROOT_NAME**    | Name of the root client                     | `root`                                 |
| **CLIENT_ROOT_ID**      | ULID for the root client                    | `01G1G1DN4JVHEKN7BHQH0F62TJ`          |
| **CLIENT_ROOT_SECRET**  | Password for the root client                | `d9keQxhgVDDF8JJLDIPZ8uq159ffOFYy`    |
| **CLIENT_ROOT_ORIGIN**  | Origin URL for the root client              | `https://localhost:8080`              |

### Standalone Mode Configuration

For standalone mode, consider the following configurations:

| **Variable Name**        | **Value**                          |
|--------------------------|-----------------------------------|
| **R2DBC_URL**           | `r2dbc:h2:mem://./tmp/`          |
| **MONGODB_EMBEDDED_ENABLE** | `true`                         |
| **REDIS_EMBEDDED_ENABLE**  | `true`                         |

## 📊 Performance Benchmark

We’ve conducted performance benchmarks in standalone mode using GitHub Actions. Below are the specifics of the testing environment and results.

### Test Environment

- **Platform:** GitHub Actions
- **Operating System:** Ubuntu 20.04
- **JDK Version:** OpenJDK 11
- **Allocated Memory:** 4 GB
- **CPU Cores:** 2 vCPUs

### Application Configuration

- **R2DBC URL:** `r2dbc:h2:mem://./tmp/`
- **Embedded MongoDB:** Enabled
- **Embedded Redis:** Enabled

### Benchmarking Tools

- **Tools Used:** k6

### Benchmarking Scenarios

We tested a range of critical functionalities, including token issuance, user retrieval, and client management. Here’s a summary of the average response times and transactions per second (TPS) categorized by HTTP method:

| Method | Endpoint                   | TPS     | Avgage   | p(50)   | p(90)   | p(95)   |
|--------|-----------------------------|---------|----------|---------|---------|---------|
| GET    | /self                        | 1041.11 | 85.56 ms | 36.04 ms| 225.25 ms| 362.71 ms|
| GET    | /clients                     | 83.47   | 1.34 s   | 1.01 s  | 3.02 s   | 4.07 s  |
| GET    | /clients/{client_id}         | 1968.06 | 85.39 ms | 82.09 ms| 129.72 ms| 148.91 ms|
| PATCH  | /clients/{client_id}         | 245.22  | 403.8 ms | 333.37 ms| 646.13 ms| 775.07 ms|
| DELETE | /clients/{client_id}         | 250.99  | 238.43 ms| 234.29 ms| 331.15 ms| 371.51 ms|
| GET    | /users                       | 125.94  | 760.71 ms| 663.44 ms| 1.28 s   | 1.41 s  |
| GET    | /users/{user-id}             | 526.45  | 115.62 ms| 76.62 ms| 143.79 ms| 424.72 ms|
| PATCH  | /users/{user-id}             | 263.72  | 369.67 ms| 300.79 ms| 527.33 ms| 1.16 s  |
| DELETE | /users/{user-id}             | 174.78  | 559.84 ms| 430.17 ms| 674.69 ms| 1.31 s  |

## 📚 Contributing

We welcome contributions to this project! If you have suggestions, improvements, or other insights, please don’t hesitate to open an issue or submit a pull request. Your input is invaluable in making this project even better!
