const mockLogs = [
  {
    id: 1,
    timestamp: "2026-10-05 10:15:21",
    level: "ERROR",
    service: "payment-service",
    message: "Database connection failed",
  },
  {
    id: 2,
    timestamp: "2026-10-05 10:16:05",
    level: "INFO",
    service: "auth-service",
    message: "User authentication successful",
  },
  {
    id: 3,
    timestamp: "2026-10-05 10:17:10",
    level: "WARN",
    service: "gateway-service",
    message: "High request latency detected",
  },
  {
    id: 4,
    timestamp: "2026-10-05 10:18:42",
    level: "INFO",
    service: "user-service",
    message: "User profile updated",
  },
  {
    id: 5,
    timestamp: "2026-10-05 10:19:30",
    level: "ERROR",
    service: "database-service",
    message: "Query execution timeout",
  },
  {
    id: 6,
    timestamp: "2026-10-05 10:20:12",
    level: "DEBUG",
    service: "gateway-service",
    message: "Incoming request processed",
  },
];

export default mockLogs;