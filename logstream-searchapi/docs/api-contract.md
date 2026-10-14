# Log Analytics Monitoring System — API Contract

## 1. Overview

This document describes the REST API and WebSocket interface provided by the Log Search API backend.

The backend provides the following functionality:

- Search logs using text and filters.
- Paginate search results.
- Aggregate logs and calculate counts.
- Group logs by fields such as log level.
- Retrieve time-based log histograms.
- Stream newly ingested logs to connected clients using WebSocket.

**Base URL (local development):**

`http://localhost:8080`

## 2. Search Logs API

### Endpoint

`GET /api/logs/search`

Searches indexed logs using optional filters and pagination.

### Query Parameters

| Parameter | Required | Description |
|---|---|---|
| `text` | No | Text to search in logs |
| `levels` | No | Set of log levels, such as INFO, WARN, ERROR |
| `services` | No | Set of service names |
| `from` | No | Start timestamp in ISO-8601 format |
| `to` | No | End timestamp in ISO-8601 format |
| `page` | No | Zero-based page number; default is 0 |
| `size` | No | Results per page; default is 50, maximum is 500 |

### Example Request

`GET /api/logs/search?text=database&levels=ERROR&page=0&size=10`

### Example Response

```json
{
  "results": [],
  "total": 0,
  "page": 0,
  "size": 10,
  "tookMs": 19
}
```

### Response Fields

| Field | Description |
|---|---|
| `results` | List of matching log records |
| `total` | Total number of matching records |
| `page` | Current zero-based page number |
| `size` | Requested page size |
| `tookMs` | Search execution time in milliseconds |

### Validation Rules

- `page` must be zero or greater.
- `size` must be between 1 and 500.
- `from` must not be later than `to`.
- Invalid query parameters return HTTP `400 Bad Request`.

### Pagination Example

First page:

`GET /api/logs/search?page=0&size=20`

Second page:

`GET /api/logs/search?page=1&size=20`

The API returns the requested page and pagination metadata.

## 3. Log Aggregation API

### Base Endpoint

`/api/logs/aggregation`

The aggregation API provides summary information about logs for dashboard analytics.

The available endpoints are count, count-by-field, and histogram.

### 3.1 Count Logs

**Endpoint:**

`GET /api/logs/aggregation/count`

Returns the count of logs matching the supplied filters.

#### Supported Query Parameters

| Parameter | Required | Description |
|---|---|---|
| `text` | No | Text filter |
| `levels` | No | Log-level filter |
| `services` | No | Service-name filter |
| `from` | No | Start timestamp |
| `to` | No | End timestamp |

#### Example Request

`GET /api/logs/aggregation/count?levels=ERROR`

The response is a numeric log count. The exact count depends on the indexed data and supplied filters.

### 3.2 Count Logs by Field

**Endpoint:**

`GET /api/logs/aggregation/count-by`

Groups matching logs by a specified field and returns the count for each group.

#### Query Parameters

| Parameter | Required | Description |
|---|---|---|
| `field` | Yes | Field used for grouping, represented by `LogField` |
| `text` | No | Text filter |
| `levels` | No | Log-level filter |
| `services` | No | Service-name filter |
| `from` | No | Start timestamp |
| `to` | No | End timestamp |

#### Example Request

`GET /api/logs/aggregation/count-by?field=LEVEL`

#### Example Response

```json
{
  "INFO": 6
}
```

This example illustrates the response shape; actual counts depend on the indexed data.

The `field` parameter must be a valid value supported by the project's `LogField` enum.

### 3.3 Log Histogram

**Endpoint:**

`GET /api/logs/aggregation/histogram`

Returns log counts grouped into time buckets.

#### Query Parameters

| Parameter | Required | Description |
|---|---|---|
| `interval` | No | Time-bucket interval in ISO-8601 duration format; default is `PT1M` |
| `text` | No | Text filter |
| `levels` | No | Log-level filter |
| `services` | No | Service-name filter |
| `from` | No | Start timestamp |
| `to` | No | End timestamp |

#### Example Request

`GET /api/logs/aggregation/histogram?interval=PT1M`

`PT1M` represents a one-minute interval.

Other examples of ISO-8601 durations include `PT5M` for five minutes and `PT1H` for one hour.

The response contains time buckets represented by the project's `TimeBucket` type. The exact JSON field names depend on that type's implementation.

## 4. WebSocket Live Tail API

### Endpoint

`ws://localhost:8080/ws/logs`

The WebSocket endpoint provides real-time delivery of newly ingested logs to connected clients.

Unlike repeated REST requests, WebSocket allows the server to push log events to clients as they arrive.

### Connection

A client establishes a WebSocket connection to:

`ws://localhost:8080/ws/logs`

The server registers the connected session and can send log messages to it.

### Incoming Messages

When a client sends a text message, the handler responds with:

`Live Tail WebSocket is working`

This is a simple connection test response.

### Outgoing Log Messages

When new logs are ingested and the ingestion event reaches the WebSocket handler, the server serializes each log record as JSON and sends it to connected, open sessions.

#### Example Log Message

```json
{
  "id": "example-id",
  "timestamp": "2026-10-14T10:00:00Z",
  "level": "INFO",
  "service": "test-service",
  "host": "localhost",
  "message": "Live Tail test log",
  "attributes": {
    "environment": "test"
  },
  "traceId": "test-trace-001"
}
```

This example illustrates a log record's structure. Actual messages use the fields serialized from the project's `LogRecord` class.

### Connection Management

The WebSocket handler:

- Registers sessions when connections are established.
- Maintains active sessions in a thread-safe session collection.
- Sends serialized log events to connected sessions that are open.
- Removes sessions when connections close.
- Handles serialization or sending `IOException` errors by logging an error message.

### Frontend Integration

The React frontend can connect to `/ws/logs`, receive JSON log events, and update its Live Tail view.

The frontend should handle connection failures and reconnect when appropriate.

## 5. Common HTTP Status Codes

| Status | Meaning |
|---|---|
| `200 OK` | Request processed successfully |
| `400 Bad Request` | Invalid query parameter or request value |

The Search API explicitly validates pagination and the timestamp range. Additional status codes and error response bodies should be documented if they are implemented by the backend.

## 6. Data Types and Conventions

- Timestamps use ISO-8601 date-time strings where applicable.
- Search pagination is zero-based.
- Search results use the `LogSearchResponse` type.
- Log search uses the `LogQuery` type.
- Aggregation operations use the project's `LogQuery`, `LogField`, and `TimeBucket` types.
- WebSocket log messages are JSON serializations of `LogRecord`.

The actual serialized fields are determined by the project's Java classes and Jackson configuration.

## 7. Local Development and Testing

### Start the Application

Start the Spring Boot application using the project's existing run configuration or Maven Wrapper.

### Test Search

`GET http://localhost:8080/api/logs/search?page=0&size=10`

### Test Aggregation

`GET http://localhost:8080/api/logs/aggregation/count`

### Test WebSocket

Connect a WebSocket client to:

`ws://localhost:8080/ws/logs`

Send a text message to verify the handler's response. To verify live log delivery, connect the client and then ingest a log through the project's ingestion mechanism.

### Run Automated Tests

From the repository root, run:

```powershell
.\mvnw.cmd -pl logstream-searchapi -am test
```

## 8. Notes for Frontend Integration

The React frontend can use the Search API to retrieve logs, the Aggregation API to retrieve summary data, and the WebSocket endpoint to receive live log events.

Before final integration, the frontend and backend teams should agree on:

- Filter parameter formats, especially how multiple levels and services are encoded.
- Exact aggregation response structures.
- Log record JSON fields.
- WebSocket connection and reconnection behavior.
- Error response formats.

This document describes the current backend interface and example requests. Any field names or response formats not explicitly confirmed against the implementation must be verified before being treated as a finalized contract.
