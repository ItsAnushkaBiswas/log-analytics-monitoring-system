# Log Analytics and Monitoring System

A Java-based log analytics and monitoring platform designed to ingest application logs, index and search log data, visualize analytics, stream logs in real time, and trigger threshold-based alerts.

## Overview

The **Log Analytics and Monitoring System** brings log ingestion, full-text search, aggregation, real-time monitoring, and alerting together in a single application.

Built using Java and Spring Boot, the system uses gRPC for log ingestion, Apache Lucene for indexing and searching, a React-based dashboard for visualization, WebSockets for live log streaming, and a scheduled alerting mechanism for threshold-based notifications.

This project demonstrates practical implementation of backend services, modular application architecture, search and indexing, real-time communication, automated testing, and performance benchmarking.

## Key Features

- **Log Ingestion:** Receive log events through gRPC services, including batch and streaming ingestion workflows.
- **Full-Text Search:** Index and search log data using Apache Lucene.
- **Log Filtering:** Query logs using supported search criteria, including log levels.
- **Log Analytics:** Generate aggregations, counts, and time-based histograms.
- **Real-Time Dashboard:** View log data and analytics through a React frontend.
- **Live Tail:** Stream incoming logs to the dashboard using WebSockets.
- **Threshold-Based Alerting:** Evaluate matching log counts against configured alert rules.
- **Scheduled Alert Evaluation:** Periodically evaluate alert rules and trigger simulated webhook notifications when conditions are met.
- **Automated Testing:** Validate ingestion, search, alerting, scheduling, and integration behavior.
- **Performance Benchmarking:** Measure ingestion throughput, search and aggregation latency, and alert evaluation performance.

## Architecture

The system follows a modular Java application architecture with a React frontend.

```text
              Log Producers / Clients
                        |
                        v
               gRPC Log Ingestion
                        |
                        v
              Log Processing & Indexing
                        |
                        v
                 Apache Lucene
                        |
             +----------+----------+
             |                     |
             v                     v
       Search & Analytics      Alert Evaluation
             |                     |
             v                     v
       REST API Services      Scheduled Rules
             |                     |
             v                     v
       React Dashboard        Simulated Webhook
             ^
             |
       WebSocket Live Tail
```

The backend modules separate shared contracts, ingestion, search and analytics, application integration, and alerting responsibilities.

## Technology Stack

| Layer | Technologies |
|---|---|
| Programming Language | Java 21 |
| Backend Framework | Spring Boot 3.5.6 |
| Log Ingestion | gRPC, Protocol Buffers |
| Search and Indexing | Apache Lucene 9.12.1 |
| API Layer | REST APIs |
| Real-Time Communication | WebSockets |
| Frontend | React, JavaScript |
| Data Visualization | ECharts |
| Build and Dependency Management | Apache Maven |
| Testing | JUnit, Mockito, Maven Surefire |
| Version Control | Git, GitHub |

## Project Structure

```text
log-analytics-monitoring-system/
├── logstream-common/
│   └── Shared contracts, search models and interfaces
├── logstream-ingestion/
│   └── Log ingestion, processing and indexing
├── logstream-searchapi/
│   └── Search APIs, controllers and analytics
├── logstream-alerting/
│   └── Alert rules, evaluation, scheduling and webhooks
├── logstream-app/
│   └── Application integration and integration tests
├── frontend/
│   └── React dashboard and frontend services
├── docs/
│   └── Alerting QA and performance documentation
├── pom.xml
├── README.md
└── .gitignore
```

## Prerequisites

Install the following software before running the project:

- JDK 21
- Apache Maven, or a configured Maven wrapper if available
- Node.js and npm
- Git
- IntelliJ IDEA or another compatible Java IDE

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/ItsAnushkaBiswas/log-analytics-monitoring-system.git
cd log-analytics-monitoring-system
```

### 2. Build and test the backend

From the project root, run:

```bash
mvn clean verify
```

This command cleans previous build outputs, compiles the Maven modules, runs the configured tests, and verifies the build.

Alternatively, open the root `pom.xml` in IntelliJ IDEA, reload the Maven project, and run the root project's **Lifecycle → verify** goal.

### 3. Start the backend

Run the Spring Boot application using the configured run configuration in IntelliJ IDEA.

Review `logstream-app/src/main/resources/application.yml` for the application's configuration and ports before starting the services.

### 4. Start the frontend

Open a separate terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the local URL printed by Vite in the terminal. Ensure the backend is running and the frontend API/WebSocket configuration matches the backend addresses.

## Testing and Quality Assurance

The project includes automated tests for important system components and integration workflows.

| Area | Validation |
|---|---|
| Alert Evaluation | Threshold-based alert decisions |
| Alert Scheduling | Scheduled evaluation and webhook behavior |
| Webhook Simulation | Notification behavior when an alert triggers |
| Log Ingestion | gRPC ingestion and batch/streaming scenarios |
| Search and Analytics | Lucene search, counts, and aggregations |
| Integration | Ingestion-to-alert workflow |
| Live Monitoring | WebSocket-based log streaming |
| Build Verification | Maven multi-module verification |

The completed Maven verification run reported **BUILD SUCCESS** across the root project and its five modules.

## Performance Benchmarking

The project includes performance tests for ingestion, search, analytics, and alerting.

Recorded benchmark results from the test environment include:

| Operation | Recorded Result |
|---|---:|
| Ingestion throughput | Approximately 88,513 logs/second |
| Ingestion of 1 million logs | Approximately 11.30 seconds |
| Full-text keyword search | Approximately 17.3 ms |
| Filtered search | Approximately 1.9 ms |
| Aggregation count | Approximately 51.1 µs/op |
| Count by field | Approximately 214.9 µs/op |
| Histogram aggregation | Approximately 1.14 ms/op |
| Alert evaluation | Approximately 31.9 ns/evaluation |

These are benchmark observations from the test environment, not guaranteed production performance. Results may vary with hardware, workload, index configuration, and test conditions.

For more details, see [`docs/alerting-qa-performance-plan.md`](docs/alerting-qa-performance-plan.md).

## Future Enhancements

Potential areas for future improvement include:

- Persistent alert-rule configuration and management
- Integration with real notification providers
- Authentication and role-based access control
- Containerized deployment and centralized configuration
- Additional load testing and production monitoring

## Project Status

**Completed project implementation and verification.**

The repository contains the modular backend, React dashboard, alerting functionality, integration tests, and QA/performance documentation.

## Repository

[GitHub — Log Analytics and Monitoring System](https://github.com/ItsAnushkaBiswas/log-analytics-monitoring-system)
