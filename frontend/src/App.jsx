import { useEffect, useMemo, useRef, useState } from "react";
import ReactECharts from "echarts-for-react";
import "./App.css";

/* ================================================================
   LOGSTREAM - MEMBER 3 DASHBOARD
   Backend integration:
   REST API  : http://localhost:8081/api/logs
   Search    : GET /api/logs/search
   Count     : GET /api/logs/aggregation/count
   Histogram  : GET /api/logs/aggregation/histogram
   Count By   : GET /api/logs/aggregation/count-by
   ================================================================ */

const API_BASE = "http://localhost:8081";

const NAV_ITEMS = [
  { label: "Dashboard", icon: "dashboard" },
  { label: "Logs", icon: "logs" },
  { label: "Analytics", icon: "analytics" },
  { label: "Live Tail", icon: "live" },
  { label: "Alerts", icon: "alerts", badge: 3 },
  { label: "Services", icon: "services" },
];

const LEVELS = ["INFO", "WARN", "ERROR", "DEBUG"];

const LEVEL_GLYPH = {
  INFO: "i",
  WARN: "!",
  ERROR: "×",
  DEBUG: "·",
};

const SERVICE_HEALTH = [
  { name: "API Gateway", uptime: "99.98%" },
  { name: "Auth Service", uptime: "99.95%" },
  { name: "User Service", uptime: "99.97%" },
  { name: "Payment Service", uptime: "99.91%" },
  { name: "Database", uptime: "99.99%" },
];

const LIVE_PREVIEW = [
  {
    time: "10:25:41",
    level: "INFO",
    service: "auth-service",
    message: "Waiting for WebSocket live stream...",
  },
  {
    time: "10:25:42",
    level: "INFO",
    service: "logstream",
    message: "Backend WebSocket endpoint available.",
  },
];

/* ================================================================
   ICONS
   ================================================================ */

const ICON_PATHS = {
  dashboard:
    "M3 3h7v9H3z M14 3h7v5h-7z M14 12h7v9h-7z M3 16h7v5H3z",

  logs: "M4 6h16 M4 12h16 M4 18h10",

  analytics:
    "M4 20V10 M10 20V4 M16 20v-7 M22 20H2",

  live:
    "M12 13a1 1 0 1 0 0-2 1 1 0 0 0 0 2z M7.8 7.8a6 6 0 0 0 0 8.4 M16.2 7.8a6 6 0 0 1 0 8.4 M4.9 4.9a10 10 0 0 0 0 14.2 M19.1 4.9a10 10 0 0 1 0 14.2",

  alerts:
    "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9 M10.3 21a1.9 1.9 0 0 0 3.4 0",

  services:
    "M12 2l9 5-9 5-9-5z M3 12l9 5 9-5 M3 17l9 5 9-5",

  settings:
    "M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z",

  search: "M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16z M21 21l-4.3-4.3",

  clock:
    "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z M12 7v5l3 2",

  chevron: "M6 9l6 6 6-6",

  arrowRight: "M5 12h14 M12 5l7 7-7 7",

  arrowUp: "M12 19V5 M5 12l7-7 7 7",

  arrowDown: "M12 5v14 M19 12l-7 7-7-7",

  errorCircle:
    "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z M12 8v4 M12 16h.01",

  warning:
    "M12 9v4 M12 17h.01 M10.3 3.9L1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z",

  checkCircle:
    "M22 11.1V12a10 10 0 1 1-5.9-9.1 M22 4L12 14l-3-3",

  close: "M6 6l12 12 M18 6L6 18",
};

function Icon({ name, size = 20 }) {
  return (
    <svg
      className="icon"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={ICON_PATHS[name]} />
    </svg>
  );
}

/* ================================================================
   HELPERS
   ================================================================ */

function getTimeRange(range) {
  const now = new Date();

  if (range === "1h") {
    return new Date(now.getTime() - 60 * 60 * 1000);
  }

  if (range === "7d") {
    return new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
  }

  return new Date(now.getTime() - 24 * 60 * 60 * 1000);
}

function normalizeLogs(data) {
  if (Array.isArray(data)) {
    return data;
  }

  if (Array.isArray(data?.results)) {
    return data.results;
  }

  if (Array.isArray(data?.content)) {
    return data.content;
  }

  return [];
}

function normalizeCount(data) {
  if (typeof data === "number") {
    return data;
  }

  if (typeof data?.count === "number") {
    return data.count;
  }

  if (typeof data?.total === "number") {
    return data.total;
  }

  if (typeof data?.value === "number") {
    return data.value;
  }

  return 0;
}

function normalizeCountBy(data) {
  if (Array.isArray(data)) {
    return data;
  }

  // The Spring Boot aggregation API returns { field, data: [...] }.
  if (Array.isArray(data?.data)) {
    return data.data;
  }

  if (Array.isArray(data?.results)) {
    return data.results;
  }

  if (Array.isArray(data?.buckets)) {
    return data.buckets;
  }

  if (data && typeof data === "object") {
    return Object.entries(data).map(([key, value]) => ({
      key,
      value:
        typeof value === "number"
          ? value
          : value?.count ?? value?.value ?? 0,
    }));
  }

  return [];
}

function normalizeHistogram(data) {
  if (Array.isArray(data)) {
    return data;
  }

  // The Spring Boot histogram API returns { interval, data: [...] }.
  if (Array.isArray(data?.data)) {
    return data.data;
  }

  if (Array.isArray(data?.buckets)) {
    return data.buckets;
  }

  if (Array.isArray(data?.results)) {
    return data.results;
  }

  return [];
}

function bucketLabel(bucket) {
  return (
    bucket?.timestamp ??
    bucket?.time ??
    bucket?.keyAsString ??
    bucket?.key ??
    bucket?.date ??
    ""
  );
}

function bucketValue(bucket) {
  return Number(
    bucket?.count ??
      bucket?.value ??
      bucket?.docCount ??
      bucket?.doc_count ??
      0
  );
}

function countByLevel(data, level) {
  const rows = normalizeCountBy(data);

  const row = rows.find((item) => {
    const key = String(
      item?.key ?? item?.level ?? item?.name ?? ""
    ).toUpperCase();

    return key === level;
  });

  return row
    ? Number(row?.value ?? row?.count ?? row?.docCount ?? 0)
    : 0;
}

/* ================================================================
   REUSABLE UI
   ================================================================ */

function LevelBadge({ level }) {
  const safeLevel = level || "INFO";

  return (
    <span className={`level-badge level-${safeLevel.toLowerCase()}`}>
      <span className="level-glyph">
        {LEVEL_GLYPH[safeLevel] || "·"}
      </span>
      {safeLevel}
    </span>
  );
}

function PanelHeader({ label, title, children }) {
  return (
    <div className="panel-header">
      <div>
        <span className="panel-label">{label}</span>
        <h2>{title}</h2>
      </div>

      {children}
    </div>
  );
}

function MetricCard({
  icon,
  label,
  value,
  trend,
  trendUp,
  tone,
}) {
  return (
    <article className={`metric-card tone-${tone}`}>
      <div className="metric-top">
        <span className="metric-label">{label}</span>

        <span className="metric-icon">
          <Icon name={icon} size={21} />
        </span>
      </div>

      <strong className="metric-value">{value}</strong>

      <span
        className={`metric-trend ${
          trendUp ? "up" : "down"
        }`}
      >
        <Icon
          name={trendUp ? "arrowUp" : "arrowDown"}
          size={14}
        />

        {trend}

        <small>live backend data</small>
      </span>
    </article>
  );
}

/* ================================================================
   SIDEBAR
   ================================================================ */

function Sidebar({ activePage, onNavigate }) {
  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">
          <Icon name="live" size={21} />
        </div>

        <div className="brand-text">
          <strong>LOGSTREAM</strong>
          <span>Distributed Observability</span>
        </div>
      </div>

      <nav className="navigation" aria-label="Main navigation">
        {NAV_ITEMS.map((item) => (
          <button
            key={item.label}
            type="button"
            className={`nav-item ${
              activePage === item.label ? "active" : ""
            }`}
            onClick={() => onNavigate(item.label)}
            title={item.label}
          >
            <Icon name={item.icon} />

            <span className="nav-label">
              {item.label}
            </span>

            {item.badge && (
              <span className="nav-badge">
                {item.badge}
              </span>
            )}
          </button>
        ))}
      </nav>

      <div className="sidebar-bottom">
        <button
          type="button"
          className={`nav-item ${
            activePage === "Settings" ? "active" : ""
          }`}
          onClick={() => onNavigate("Settings")}
          title="Settings"
        >
          <Icon name="settings" />
          <span className="nav-label">Settings</span>
        </button>

        <div className="user-card">
          <div className="avatar">M3</div>

          <div className="user-text">
            <strong>Member 3</strong>
            <span>Dashboard Developer</span>
          </div>
        </div>
      </div>
    </aside>
  );
}

/* ================================================================
   TOPBAR
   ================================================================ */

function Topbar({ title, backendConnected }) {
  return (
    <header className="topbar">
      <div className="page-title">
        <span className="page-kicker">
          OBSERVABILITY PLATFORM
        </span>

        <h1>{title}</h1>

        <p>Real-time distributed log analytics</p>
      </div>

      <div className="top-actions">
        <span className="system-status">
          <i
            className={`status-dot ${
              backendConnected ? "" : "status-offline"
            }`}
          />

          {backendConnected
            ? "Backend Connected"
            : "Backend Offline"}
        </span>

        <button
          type="button"
          className="ghost-button time-button"
        >
          <Icon name="clock" size={17} />
          Live Backend
          <Icon name="chevron" size={15} />
        </button>

        <button
          type="button"
          className="icon-button"
          aria-label="Notifications"
        >
          <Icon name="alerts" size={19} />
          <span className="notif-dot" />
        </button>

        <div
          className="profile"
          role="img"
          aria-label="Profile: Member 3"
        >
          M3
        </div>
      </div>
    </header>
  );
}

/* ================================================================
   FILTER TOOLBAR
   ================================================================ */

function FilterToolbar({
  search,
  setSearch,
  level,
  setLevel,
  service,
  setService,
  services,
  timeRange,
  setTimeRange,
  onSearch,
  onClear,
  searchRef,
  loading,
}) {
  return (
    <form
      className="filter-toolbar glass-panel"
      role="search"
      onSubmit={(event) => {
        event.preventDefault();
        onSearch();
      }}
    >
      <div className="search-row">
        <label className="search-field">
          <span className="sr-only">
            Search logs
          </span>

          <Icon name="search" size={20} />

          <input
            ref={searchRef}
            type="search"
            value={search}
            onChange={(event) =>
              setSearch(event.target.value)
            }
            placeholder="Search logs, services, or keywords..."
          />

          <kbd>Ctrl K</kbd>
        </label>

        <button
          type="button"
          className="clear-icon-button"
          onClick={onClear}
          aria-label="Clear filters"
          title="Clear filters"
        >
          <Icon name="close" size={20} />
        </button>
      </div>

      <div className="filter-section">
        <div className="filter-heading">
          <span>FILTERS</span>
          <div className="filter-heading-line" />
        </div>

        <div className="filter-row">
          <label className="select-field neon-filter neon-blue">
            <span className="select-label">
              Level
            </span>

            <select
              value={level}
              onChange={(event) =>
                setLevel(event.target.value)
              }
            >
              <option value="ALL">
                All Levels
              </option>

              {LEVELS.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>

          <label className="select-field neon-filter neon-violet">
            <span className="select-label">
              Service
            </span>

            <select
              value={service}
              onChange={(event) =>
                setService(event.target.value)
              }
            >
              <option value="ALL">
                All Services
              </option>

              {services.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>

          <label className="select-field neon-filter neon-cyan">
            <span className="select-label">
              Time Range
            </span>

            <select
              value={timeRange}
              onChange={(event) =>
                setTimeRange(event.target.value)
              }
            >
              <option value="1h">
                Last Hour
              </option>

              <option value="24h">
                Last 24 Hours
              </option>

              <option value="7d">
                Last 7 Days
              </option>
            </select>
          </label>

          <button
            type="submit"
            className="filter-search-button"
            disabled={loading}
          >
            <Icon name="search" size={17} />

            {loading ? "Loading..." : "Search"}
          </button>
        </div>
      </div>
    </form>
  );
}

/* ================================================================
   LOG ACTIVITY CHART
   ================================================================ */

function LogActivityChart({ histogram }) {
  const normalized = normalizeHistogram(histogram);

  const labels = normalized.map(bucketLabel);
  const values = normalized.map(bucketValue);

  const hasData =
    labels.length > 0 &&
    values.some((value) => value > 0);

  const option = useMemo(
    () => ({
      backgroundColor: "transparent",

      animation: true,
      animationDuration: 500,

      grid: {
        left: 8,
        right: 12,
        top: 48,
        bottom: 10,
        containLabel: true,
      },

      tooltip: {
        trigger: "axis",
        confine: true,

        backgroundColor: "rgba(8,13,31,0.97)",
        borderColor: "rgba(91,140,255,0.35)",
        borderWidth: 1,

        padding: [10, 14],

        textStyle: {
          color: "#e8ecf8",
          fontSize: 13,
        },
      },

      legend: {
        show: false,
      },

      xAxis: {
        type: "category",
        boundaryGap: false,
        data: labels,

        axisLine: {
          lineStyle: {
            color: "rgba(148,163,255,0.16)",
          },
        },

        axisTick: {
          show: false,
        },

        axisLabel: {
          color: "#8e9ac0",
          fontSize: 10,
          margin: 12,
          formatter: (value) => {
            const text = String(value);

            if (text.length > 16) {
              return text.slice(11, 16);
            }

            return text;
          },
        },
      },

      yAxis: {
        type: "value",

        axisLine: {
          show: false,
        },

        axisTick: {
          show: false,
        },

        axisLabel: {
          color: "#8e9ac0",
          fontSize: 11,
        },

        splitLine: {
          lineStyle: {
            color: "rgba(148,163,255,0.075)",
            type: "dashed",
          },
        },
      },

      series: [
        {
          name: "Logs",
          type: "line",
          smooth: 0.4,
          showSymbol: false,
          data: values,

          lineStyle: {
            width: 3,
            color: "#5b8cff",
          },

          itemStyle: {
            color: "#5b8cff",
          },

          areaStyle: {
            color: {
              type: "linear",
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,

              colorStops: [
                {
                  offset: 0,
                  color: "rgba(91,140,255,0.30)",
                },
                {
                  offset: 0.55,
                  color: "rgba(91,140,255,0.10)",
                },
                {
                  offset: 1,
                  color: "rgba(91,140,255,0)",
                },
              ],
            },
          },
        },
      ],
    }),
    [labels, values]
  );

  return (
    <section className="glass-panel chart-panel">
      <div className="panel-header">
        <div>
          <span className="panel-label">
            Analytics
          </span>

          <h2>Log Activity</h2>

          <p className="panel-sub">
            Backend aggregation over time
          </p>
        </div>

        <span className="pill pill-green">
          REST API
        </span>
      </div>

      <div className="chart-area">
        {hasData ? (
          <ReactECharts
            option={option}
            style={{
              width: "100%",
              height: "100%",
            }}
            opts={{
              renderer: "canvas",
              devicePixelRatio:
                window.devicePixelRatio || 1,
            }}
            notMerge
            lazyUpdate
          />
        ) : (
          <div className="empty-state">
            <Icon name="analytics" size={28} />
            <strong>No aggregation data</strong>
            <span>
              Start the backend and ingest logs.
            </span>
          </div>
        )}
      </div>
    </section>
  );
}

/* ================================================================
   LEVEL DISTRIBUTION
   ================================================================ */

function LevelDistribution({
  countByData,
  total,
}) {
  const rows = LEVELS.map((level) => {
    const count = countByLevel(
      countByData,
      level
    );

    return {
      level,
      count,
      pct: total
        ? (count / total) * 100
        : 0,
    };
  });

  let accumulated = 0;

  const stops = rows
    .filter((row) => row.count > 0)
    .map((row) => {
      const start = accumulated;

      accumulated += row.pct;

      return `var(--c-${row.level.toLowerCase()}) ${start}% ${accumulated}%`;
    });

  const donutBackground = total
    ? `conic-gradient(${stops.join(", ")})`
    : "conic-gradient(rgba(255,255,255,0.08) 0 100%)";

  return (
    <section className="glass-panel levels-panel">
      <PanelHeader
        label="Distribution"
        title="Log Levels"
      />

      <div className="levels-body">
        <div
          className="donut"
          style={{
            background: donutBackground,
          }}
        >
          <div className="donut-hole">
            <strong>
              {Number(total || 0).toLocaleString()}
            </strong>

            <span>Matching logs</span>
          </div>
        </div>

        <ul className="level-list">
          {rows.map((row) => (
            <li key={row.level}>
              <div className="level-list-top">
                <LevelBadge level={row.level} />

                <span className="level-count">
                  {row.count.toLocaleString()}{" "}
                  <small>
                    ({row.pct.toFixed(0)}%)
                  </small>
                </span>
              </div>

              <div className="bar-track">
                <div
                  className={`bar-fill bar-${row.level.toLowerCase()}`}
                  style={{
                    width: `${row.pct}%`,
                  }}
                />
              </div>
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}

/* ================================================================
   LOG TABLE
   ================================================================ */

function LogTable({ logs, loading }) {
  return (
    <section className="glass-panel logs-panel">
      <PanelHeader
        label="Backend results"
        title="Recent Logs"
      >
        <span className="record-count">
          {logs.length.toLocaleString()}{" "}
          {logs.length === 1
            ? "record"
            : "records"}
        </span>
      </PanelHeader>

      <div className="table-scroll">
        <table className="log-table">
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Level</th>
              <th>Service</th>
              <th>Message</th>
            </tr>
          </thead>

          <tbody>
            {logs.map((log, index) => (
              <tr
                key={
                  log.id ??
                  `${log.timestamp}-${index}`
                }
              >
                <td className="cell-time">
                  {log.timestamp || "-"}
                </td>

                <td>
                  <LevelBadge
                    level={log.level}
                  />
                </td>

                <td>
                  <span className="service-badge">
                    {log.service || "-"}
                  </span>
                </td>

                <td className="cell-message">
                  {log.message || "-"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {loading && (
          <div className="empty-state">
            <strong>Loading logs...</strong>
            <span>
              Fetching data from the Java backend.
            </span>
          </div>
        )}

        {!loading && logs.length === 0 && (
          <div className="empty-state">
            <Icon name="search" size={28} />

            <strong>
              No logs returned by backend
            </strong>

            <span>
              Check the Java backend and ingestion
              service.
            </span>
          </div>
        )}
      </div>
    </section>
  );
}

/* ================================================================
   SERVICE HEALTH
   ================================================================ */

function ServiceHealth() {
  return (
    <section className="glass-panel">
      <PanelHeader
        label="Infrastructure"
        title="Service Health"
      >
        <span className="pill pill-green">
          <i className="status-dot" />
          Healthy
        </span>
      </PanelHeader>

      <ul className="service-list">
        {SERVICE_HEALTH.map((service) => (
          <li key={service.name}>
            <span className="service-name">
              <i className="status-dot" />
              {service.name}
            </span>

            <span className="service-meta">
              <span className="service-state">
                Operational
              </span>

              <strong>
                {service.uptime}
              </strong>
            </span>
          </li>
        ))}
      </ul>
    </section>
  );
}

/* ================================================================
   LIVE TAIL PREVIEW
   ================================================================ */

function LiveTailPreview({ onOpen }) {
  return (
    <section className="glass-panel live-panel">
      <PanelHeader
        label="Real-time"
        title="Live Tail"
      >
        <span className="pill pill-green">
          <i className="status-dot pulse" />
          WebSocket Ready
        </span>
      </PanelHeader>

      <div
        className="terminal"
        role="log"
        aria-label="Live log preview"
      >
        {LIVE_PREVIEW.map(
          (entry, index) => (
            <div
              className="terminal-row"
              key={`${entry.time}-${index}`}
            >
              <div className="terminal-meta">
                <time>{entry.time}</time>

                <LevelBadge
                  level={entry.level}
                />

                <span className="terminal-service">
                  {entry.service}
                </span>
              </div>

              <p>{entry.message}</p>
            </div>
          )
        )}
      </div>

      <button
        type="button"
        className="link-button"
        onClick={onOpen}
      >
        Open Live Tail
        <Icon name="arrowRight" size={18} />
      </button>
    </section>
  );
}

/* ================================================================
   MAIN APP
   ================================================================ */

function App() {
  const [search, setSearch] = useState("");
  const [level, setLevel] = useState("ALL");
  const [service, setService] = useState("ALL");
  const [timeRange, setTimeRange] =
    useState("24h");

  const [activePage, setActivePage] =
    useState("Dashboard");

  const [logs, setLogs] = useState([]);
  const [totalLogs, setTotalLogs] =
    useState(0);

  const [countByData, setCountByData] =
    useState([]);

  const [histogram, setHistogram] =
    useState([]);

  const [services, setServices] =
    useState([]);

  const [loading, setLoading] =
    useState(false);

  const [backendConnected, setBackendConnected] =
    useState(false);

  const [error, setError] =
    useState("");

  const searchRef = useRef(null);

  /* --------------------------------------------------------------
     Ctrl + K
     -------------------------------------------------------------- */

  useEffect(() => {
    function handleKeyboard(event) {
      if (
        (event.ctrlKey || event.metaKey) &&
        event.key.toLowerCase() === "k"
      ) {
        event.preventDefault();
        searchRef.current?.focus();
      }
    }

    window.addEventListener(
      "keydown",
      handleKeyboard
    );

    return () =>
      window.removeEventListener(
        "keydown",
        handleKeyboard
      );
  }, []);

  /* --------------------------------------------------------------
     BACKEND REQUEST
     -------------------------------------------------------------- */

  async function fetchDashboardData() {
    setLoading(true);
    setError("");

    try {
      const params = new URLSearchParams();

      if (search.trim()) {
        params.set(
          "text",
          search.trim()
        );
      }

      if (level !== "ALL") {
        params.set("levels", level);
      }

      if (service !== "ALL") {
        params.set(
          "services",
          service
        );
      }

      const fromDate =
        getTimeRange(timeRange);

      params.set(
        "from",
        fromDate.toISOString()
      );

      params.set(
        "to",
        new Date().toISOString()
      );

      params.set("page", "0");
      params.set("size", "50");

      /* Search API */
      const searchResponse =
        await fetch(
          `${API_BASE}/api/logs/search?${params.toString()}`
        );

      if (!searchResponse.ok) {
        throw new Error(
          `Search API returned ${searchResponse.status}`
        );
      }

      const searchData =
        await searchResponse.json();

      const resultLogs =
        normalizeLogs(searchData);

      setLogs(resultLogs);

      /* Extract services from real backend results */
      const discoveredServices = [
        ...new Set(
          resultLogs
            .map((log) => log.service)
            .filter(Boolean)
        ),
      ].sort();

      setServices(
        discoveredServices
      );

      /* Count API */
      const countResponse =
        await fetch(
          `${API_BASE}/api/logs/aggregation/count`
        );

      if (countResponse.ok) {
        const countData =
          await countResponse.json();

        setTotalLogs(
          normalizeCount(countData)
        );
      } else {
        setTotalLogs(
          Number(
            searchData?.total ??
              resultLogs.length
          )
        );
      }

      /* Count By LEVEL */
      const countByResponse =
        await fetch(
          `${API_BASE}/api/logs/aggregation/count-by?field=LEVEL`
        );

      if (countByResponse.ok) {
        const countByJson =
          await countByResponse.json();

        setCountByData(
          countByJson
        );
      }

      /* Histogram */
      const histogramResponse =
        await fetch(
          `${API_BASE}/api/logs/aggregation/histogram?interval=PT1M`
        );

      if (histogramResponse.ok) {
        const histogramJson =
          await histogramResponse.json();

        setHistogram(
          normalizeHistogram(
            histogramJson
          )
        );
      }

      setBackendConnected(true);
    } catch (requestError) {
      console.error(
        "LogStream backend error:",
        requestError
      );

      setBackendConnected(false);

      setError(
        requestError?.message ||
          "Unable to connect to backend"
      );

      setLogs([]);
      setHistogram([]);
      setCountByData([]);
      setTotalLogs(0);
    } finally {
      setLoading(false);
    }
  }

  /* --------------------------------------------------------------
     INITIAL LOAD
     -------------------------------------------------------------- */
useEffect(() => {
  const loadInitialDashboard = async () => {
    await fetchDashboardData();
  };

  loadInitialDashboard();

  // Initial dashboard load intentionally runs once.
  // Filter changes are handled by the Search button.
  // eslint-disable-next-line react-hooks/exhaustive-deps
}, []);
  /* --------------------------------------------------------------
     CLEAR FILTERS
     -------------------------------------------------------------- */

  function clearFilters() {
    setSearch("");
    setLevel("ALL");
    setService("ALL");
    setTimeRange("24h");

    requestAnimationFrame(() => {
      searchRef.current?.focus();
    });
  }

  /* --------------------------------------------------------------
     METRICS
     -------------------------------------------------------------- */

  const errors = countByLevel(
    countByData,
    "ERROR"
  );

  const warnings = countByLevel(
    countByData,
    "WARN"
  );

  const activeServices =
    services.length ||
    new Set(
      logs
        .map((log) => log.service)
        .filter(Boolean)
    ).size;

  /* --------------------------------------------------------------
     RENDER
     -------------------------------------------------------------- */

  return (
    <div className="app-shell">
      <Sidebar
        activePage={activePage}
        onNavigate={setActivePage}
      />

      <main className="main-content">
        <Topbar
          title={activePage}
          backendConnected={
            backendConnected
          }
        />

        {error && (
          <div className="backend-error glass-panel">
            <strong>
              Backend connection error
            </strong>

            <span>{error}</span>

            <button
              type="button"
              onClick={fetchDashboardData}
            >
              Retry
            </button>
          </div>
        )}

        <section
          className="metrics-grid"
          aria-label="Key metrics"
        >
          <MetricCard
            icon="logs"
            label="TOTAL LOGS"
            value={Number(
              totalLogs
            ).toLocaleString()}
            trend="Live"
            trendUp
            tone="blue"
          />

          <MetricCard
            icon="errorCircle"
            label="ERRORS"
            value={Number(
              errors
            ).toLocaleString()}
            trend="Live"
            trendUp={false}
            tone="red"
          />

          <MetricCard
            icon="warning"
            label="WARNINGS"
            value={Number(
              warnings
            ).toLocaleString()}
            trend="Live"
            trendUp={false}
            tone="amber"
          />

          <MetricCard
            icon="checkCircle"
            label="ACTIVE SERVICES"
            value={Number(
              activeServices
            ).toLocaleString()}
            trend="Live"
            trendUp
            tone="green"
          />
        </section>

        <FilterToolbar
          search={search}
          setSearch={setSearch}
          level={level}
          setLevel={setLevel}
          service={service}
          setService={setService}
          services={services}
          timeRange={timeRange}
          setTimeRange={setTimeRange}
          onSearch={
            fetchDashboardData
          }
          onClear={clearFilters}
          searchRef={searchRef}
          loading={loading}
        />

        <section className="analytics-grid">
          <LogActivityChart
            histogram={histogram}
          />

          <LevelDistribution
            countByData={countByData}
            total={totalLogs}
          />
        </section>

        <section className="lower-grid">
          <LogTable
            logs={logs}
            loading={loading}
          />

          <div className="right-column">
            <ServiceHealth />

            <LiveTailPreview
              onOpen={() =>
                setActivePage(
                  "Live Tail"
                )
              }
            />
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;