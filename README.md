# Log Analytics and Monitoring System

A Java-based log analytics and monitoring platform for centralized log ingestion, indexing, searching, analytics, real-time monitoring, and threshold-based alerting.

## Project Overview

The Log Analytics and Monitoring System provides a centralized solution for processing and monitoring application logs. It combines a Java backend with Apache Lucene for search and indexing, a React dashboard for visualization, and alerting capabilities to help identify important events.

### Key Features

- **Log Ingestion:** Receive application logs through gRPC.
- **Log Indexing and Search:** Index and search log data using Apache Lucene.
- **Log Analytics:** Perform aggregations and analyze log patterns.
- **Monitoring Dashboard:** Visualize log information through a React-based interface.
- **Data Visualization:** Display analytics using ECharts.
- **Real-Time Live Tail:** Stream log updates using WebSockets.
- **Threshold-Based Alerting:** Evaluate alert rules against log counts.
- **Scheduled Alert Evaluation:** Execute alert checks periodically.
- **Simulated Webhook Notifications:** Simulate notifications when alert conditions are met.
- **Testing and Performance Validation:** Validate functionality through automated tests and performance benchmarks.

## Technology Stack

| Component | Technologies |
|---|---|
| Programming Language | Java 21 |
| Backend Framework | Spring Boot |
| Log Ingestion | gRPC, Protocol Buffers |
| Indexing and Search | Apache Lucene |
| API Layer | REST APIs |
| Real-Time Communication | WebSockets |
| Frontend | React |
| Data Visualization | ECharts |
| Build Management | Maven |
| Testing | JUnit, Mockito |
| Version Control | Git, GitHub |

## System Architecture

```text
             Application Log Producers
                       |
                       v
                gRPC Ingestion
                       |
                       v
              Log Processing Pipeline
                       |
                       v
                Apache Lucene
                Index and Search
                       |
             +---------+---------+
             |                   |
             v                   v
      Search and Analytics   Alert Evaluation
             |                   |
             v                   v
       REST API Layer       Scheduled Rules
             |                   |
             v                   v
      React Dashboard      Simulated Webhook

        Real-Time Log Updates
                 |
                 v
             WebSockets
                 |
                 v
          Live Tail Dashboard
```

## Project Structure

```text
log-analytics-monitoring-system/
├── pom.xml
├── logstream-common/
│   └── src/main/
│       ├── proto/
│       └── java/com/logstream/common/
│           ├── dto/
│           └── search/
├── logstream-ingestion/
│   └── src/main/java/com/logstream/ingestion/
│       ├── grpc/
│       ├── parser/
│       ├── lucene/
│       ├── service/
│       └── config/
├── logstream-searchapi/
│   └── src/main/java/com/logstream/searchapi/
│       ├── controller/
│       ├── dto/
│       ├── websocket/
│       ├── service/
│       └── config/
├── logstream-alerting/
│   └── src/main/java/com/logstream/alerting/
│       ├── scheduler/
│       ├── rules/
│       ├── webhook/
│       └── config/
├── logstream-app/
│   └── src/main/java/com/logstream/
│       └── LogStreamApplication.java
├── frontend/
│   ├── package.json
│   ├── src/
│   └── public/
└── docs/
    └── alerting-qa-performance-plan.md
```

*Note: The structure above summarizes the main modules and directories. Individual files may vary depending on the current repository contents.*

## Getting Started

### Prerequisites

- JDK 21
- Git
- Maven, or the Maven Wrapper if included in the repository
- Node.js and npm for the frontend
- IntelliJ IDEA or another Java IDE (optional)

Verify your Java installation:

```bash
java -version
```

### 1. Clone the Repository

```bash
git clone https://github.com/ItsAnushkaBiswas/log-analytics-monitoring-system.git
cd log-analytics-monitoring-system
```

### 2. Build and Test the Backend

If Maven is installed:

```bash
mvn clean verify
```

If the repository includes the Maven Wrapper, use the appropriate command for your operating system.

**Windows:**

```bat
mvnw.cmd clean verify
```

**macOS/Linux:**

```bash
./mvnw clean verify
```

The verification process builds the Maven modules and runs the configured automated tests.

### 3. Run the Backend

After a successful build, run the packaged application:

```bash
java -jar logstream-app/target/logstream-app-1.0-SNAPSHOT.jar
```

Alternatively, open the project in IntelliJ IDEA, reload the Maven project, and run `LogStreamApplication.java` from the application module.

Ensure the required application configuration and module dependencies are available before starting the application.

### 4. Run the Frontend

Open a terminal in the frontend directory:

```bash
cd frontend
npm install
npm run dev
```

Open the local development URL printed by the frontend development server.

Ensure the frontend's API and WebSocket configuration points to the running backend.

## Sending a Test Log

The gRPC ingestion service is configured to listen on port `9090` by default in the documented setup.

You can use Postman's gRPC request interface or `grpcurl` to send a test log, provided the service definition and server configuration match the command.

Example using `grpcurl`:

```bash
grpcurl -plaintext \
  -d '{"level":"ERROR","service":"payment","message":"card declined"}' \
  localhost:9090 \
  logstream.v1.LogIngestionService/Ingest
```

This example assumes that the server exposes the specified gRPC service and method and that the request matches the Protocol Buffers schema.

## Testing and Validation

The project includes automated tests and validation across key components.

| Area | Validation |
|---|---|
| Alert Rules | Rule evaluation and threshold conditions |
| Alert Scheduling | Scheduled evaluation behavior |
| Webhook | Simulated notification behavior |
| Log Ingestion | Ingestion processing and gRPC-related tests |
| Search and Analytics | Search, aggregation, and counting behavior |
| Integration | Ingestion-to-alerting integration scenarios |
| Real-Time Monitoring | Live Tail and WebSocket-related functionality |
| Build Verification | Maven reactor build and test verification |

Run the configured backend tests using:

```bash
mvn clean verify
```

## Performance Results

The following figures are from the project's reported test and benchmark runs. They are indicative results from the tested environment, not guaranteed production performance.

| Metric | Reported Result |
|---|---:|
| Log Ingestion Throughput | Approximately 88,513 logs/second |
| Processing 1 Million Logs | Approximately 11.30 seconds |
| Full-Text Keyword Search | Approximately 17.3 ms |
| Filtered Search | Approximately 1.9 ms |
| Lucene Count Aggregation | Approximately 51.1 µs/operation |
| Count-by-Field Aggregation | Approximately 214.9 µs/operation |
| Histogram Aggregation | Approximately 1.14 ms/operation |
| Alert Evaluation | Approximately 31.9 ns/evaluation |

Actual performance may vary depending on hardware, dataset size, index configuration, JVM settings, and workload.

For additional alerting and performance validation details, see [`docs/alerting-qa-performance-plan.md`](docs/alerting-qa-performance-plan.md).

## Future Enhancements

Potential improvements include:

- Persistent storage and management for alert rules.
- Integration with real notification providers and webhook endpoints.
- Authentication and role-based access control.
- Containerization and deployment automation.
- Expanded load testing and production-scale benchmarking.
- Enhanced dashboard filtering and monitoring capabilities.

## Project Status

**Implementation and verification completed** for the project's defined scope. See the test and performance documentation for validation details.

## Repository

GitHub: [Log Analytics and Monitoring System](https://github.com/ItsAnushkaBiswas/log-analytics-monitoring-system)
