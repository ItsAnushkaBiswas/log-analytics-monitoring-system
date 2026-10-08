# Log Analytics Monitoring System

A monolithic Java-based log analytics and monitoring system for
centralized log ingestion, indexing, searching, analytics,
real-time monitoring, and alerting.

## Project Overview

The system provides the following major capabilities:

- Log ingestion using gRPC
- Log indexing and search using Apache Lucene
- Log analytics and aggregations
- React-based monitoring dashboard
- Data visualization using ECharts
- Real-time Live Tail using WebSockets
- Scheduled alerting
- Simulated webhook notification
- Testing and performance validation

## Technology Stack

### Backend
- Java
- Spring Boot
- gRPC
- Apache Lucene

### Frontend
- React
- ECharts

### Real-Time Communication
- WebSocket

## Project Structure

```text
log-analytics-monitoring-system/
├── pom.xml                          # parent POM — module list, shared deps/versions
│
├── logstream-common/                # ⚠️ shared contracts — changes need review from all
│   ├── pom.xml
│   ├── src/main/proto/log_message.proto  # shared .proto schema (M1 owns edits, all consume)
│   └── src/main/java/com/logstream/common/
│       ├── dto/                     # shared DTOs used across modules (e.g. LogRecord)
│       └── search/                  # LogSearchService interface + query/result types
│
├── logstream-ingestion/             # Member 1 — Ingestion & Lucene Core
│   ├── pom.xml
│   └── src/main/java/com/logstream/ingestion/
│       ├── grpc/                    # gRPC server, generated stubs
│       ├── parser/                  # log parsing logic
│       ├── lucene/                  # index writer, document builder, search implementation
│       ├── service/                 # parse → index → notify pipeline
│       └── config/
│
├── logstream-searchapi/             # Member 2 — Search API & WebSocket
│   ├── pom.xml
│   └── src/main/java/com/logstream/searchapi/
│       ├── controller/              # REST controllers
│       ├── dto/                     # request/response DTOs
│       ├── websocket/               # Live Tail handler
│       ├── service/                 # calls into ingestion module's search interface
│       └── config/
│
├── logstream-alerting/              # Member 4 — Alerting, Testing & Performance
│   ├── pom.xml
│   └── src/main/java/com/logstream/alerting/
│       ├── com.logstream.alerting.scheduler/               # @Scheduled rule evaluator
│       ├── rules/                   # rule definitions, threshold logic
│       ├── webhook/                 # simulated webhook client
│       └── config/
│
├── logstream-app/                   # ⚠️ assembly module — wires everything into one boot jar
│   ├── pom.xml
│   └── src/main/java/com/logstream/
│       └── LogStreamApplication.java   # single @SpringBootApplication entry point
│
└── logstream-dashboard/             # Member 3 — React frontend (separate build entirely)
    ├── package.json
    ├── src/
    │   ├── components/
    │   ├── pages/
    │   ├── api/                     # REST/WebSocket client
    │   └── charts/                  # ECharts views
    └── public/
```

## Getting Started

Prerequisites: **JDK 21** (`JAVA_HOME` must point to it) and Git. Maven is not needed — use the wrapper.

```bash
./mvnw clean install
java -jar logstream-app/target/logstream-app-1.0-SNAPSHOT.jar
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

The app listens for logs over gRPC on port **9090** and stores the index in `./index-data`
(override with `LOGSTREAM_GRPC_PORT` / `LOGSTREAM_INDEX_PATH`).

Send a test log with Postman (New → gRPC → `localhost:9090`, "Use server reflection") or grpcurl:

```bash
grpcurl -plaintext -d '{"level":"ERROR","service":"payment","message":"card declined"}' localhost:9090 logstream.v1.LogIngestionService/Ingest
```
