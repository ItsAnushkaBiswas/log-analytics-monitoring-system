import { useEffect, useMemo, useRef, useState } from "react";
import ReactECharts from "echarts-for-react";
import mockLogs from "./data/mockLogs";
import "./App.css";

/* ------------------------------------------------------------------ */
/* Constants                                                          */
/* ------------------------------------------------------------------ */

const NAV_ITEMS = [
  { label: "Dashboard", icon: "dashboard" },
  { label: "Logs", icon: "logs" },
  { label: "Analytics", icon: "analytics" },
  { label: "Live Tail", icon: "live" },
  { label: "Alerts", icon: "alerts", badge: 3 },
  { label: "Services", icon: "services" },
];

const LEVELS = ["INFO", "WARN", "ERROR", "DEBUG"];

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
    message: "User authentication successful",
  },
  {
    time: "10:25:42",
    level: "ERROR",
    service: "payment-service",
    message: "Database connection failed",
  },
  {
    time: "10:25:43",
    level: "WARN",
    service: "gateway-service",
    message: "High request latency detected",
  },
  {
    time: "10:25:44",
    level: "INFO",
    service: "user-service",
    message: "User profile updated",
  },
];

const LEVEL_GLYPH = {
  INFO: "i",
  WARN: "!",
  ERROR: "×",
  DEBUG: "·",
};

/* ------------------------------------------------------------------ */
/* Icons                                                              */
/* ------------------------------------------------------------------ */

const ICON_PATHS = {
  dashboard:
    "M3 3h7v9H3z M14 3h7v5h-7z M14 12h7v9h-7z M3 16h7v5H3z",

  logs:
    "M4 6h16 M4 12h16 M4 18h10",

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

  search:
    "M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16z M21 21l-4.3-4.3",

  clock:
    "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z M12 7v5l3 2",

  chevron:
    "M6 9l6 6 6-6",

  arrowRight:
    "M5 12h14 M12 5l7 7-7 7",

  arrowUp:
    "M12 19V5 M5 12l7-7 7 7",

  arrowDown:
    "M12 5v14 M19 12l-7 7-7-7",

  errorCircle:
    "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z M12 8v4 M12 16h.01",

  warning:
    "M12 9v4 M12 17h.01 M10.3 3.9L1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z",

  checkCircle:
    "M22 11.1V12a10 10 0 1 1-5.9-9.1 M22 4L12 14l-3-3",

  close:
    "M6 6l12 12 M18 6L6 18",
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

/* ------------------------------------------------------------------ */
/* Reusable UI                                                        */
/* ------------------------------------------------------------------ */

function LevelBadge({ level }) {
  return (
    <span className={`level-badge level-${level.toLowerCase()}`}>
      <span className="level-glyph" aria-hidden="true">
        {LEVEL_GLYPH[level] || "·"}
      </span>
      {level}
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

        <small>vs previous period</small>
      </span>
    </article>
  );
}

/* ------------------------------------------------------------------ */
/* Sidebar                                                            */
/* ------------------------------------------------------------------ */

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

      <nav
        className="navigation"
        aria-label="Main navigation"
      >
        {NAV_ITEMS.map((item) => (
          <button
            key={item.label}
            type="button"
            className={`nav-item ${
              activePage === item.label ? "active" : ""
            }`}
            onClick={() => onNavigate(item.label)}
            aria-current={
              activePage === item.label
                ? "page"
                : undefined
            }
            title={item.label}
          >
            <Icon name={item.icon} />

            <span className="nav-label">
              {item.label}
            </span>

            {item.badge && (
              <span
                className="nav-badge"
                aria-label={`${item.badge} new alerts`}
              >
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
            activePage === "Settings"
              ? "active"
              : ""
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

/* ------------------------------------------------------------------ */
/* Topbar                                                             */
/* ------------------------------------------------------------------ */

function Topbar({ title }) {
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
            className="status-dot"
            aria-hidden="true"
          />
          All Systems Operational
        </span>

        <button
          type="button"
          className="ghost-button time-button"
        >
          <Icon name="clock" size={17} />
          Last 24 Hours
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

/* ------------------------------------------------------------------ */
/* Search + Filters                                                   */
/* ------------------------------------------------------------------ */

function FilterToolbar({
  search,
  setSearch,
  level,
  setLevel,
  service,
  setService,
  services,
  onClear,
  searchRef,
}) {
  return (
    <form
      className="filter-toolbar glass-panel"
      role="search"
      aria-label="Filter logs"
      onSubmit={(event) => event.preventDefault()}
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
              {services.map((item) => (
                <option key={item} value={item}>
                  {item === "ALL"
                    ? "All Services"
                    : item}
                </option>
              ))}
            </select>
          </label>

          <label className="select-field neon-filter neon-cyan">
            <span className="select-label">
              Time Range
            </span>

            <select defaultValue="24h">
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
          >
            <Icon name="search" size={17} />
            Search
          </button>
        </div>
      </div>
    </form>
  );
}

/* ------------------------------------------------------------------ */
/* ECharts — Log Activity                                             */
/* ------------------------------------------------------------------ */

function LogActivityChart() {
  const hours = [
    "00:00",
    "02:00",
    "04:00",
    "06:00",
    "08:00",
    "10:00",
    "12:00",
    "14:00",
    "16:00",
    "18:00",
    "20:00",
    "22:00",
  ];

  const series = [
    {
      name: "INFO",
      color: "#5b8cff",
      data: [
        320,
        280,
        240,
        310,
        560,
        820,
        940,
        880,
        760,
        690,
        520,
        410,
      ],
    },
    {
      name: "WARN",
      color: "#fbbf24",
      data: [
        40,
        35,
        30,
        48,
        90,
        130,
        150,
        140,
        120,
        110,
        80,
        60,
      ],
    },
    {
      name: "ERROR",
      color: "#f87171",
      data: [
        12,
        9,
        8,
        15,
        30,
        44,
        52,
        47,
        38,
        33,
        25,
        18,
      ],
    },
    {
      name: "DEBUG",
      color: "#8b6dff",
      data: [
        90,
        80,
        70,
        95,
        140,
        200,
        230,
        210,
        180,
        160,
        130,
        110,
      ],
    },
  ];

  const option = useMemo(
    () => ({
      backgroundColor: "transparent",

      animation: true,
      animationDuration: 700,

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

        backgroundColor:
          "rgba(8,13,31,0.97)",

        borderColor:
          "rgba(91,140,255,0.35)",

        borderWidth: 1,

        padding: [10, 14],

        textStyle: {
          color: "#e8ecf8",
          fontSize: 13,
        },

        axisPointer: {
          type: "line",

          lineStyle: {
            color:
              "rgba(60,200,255,0.35)",
          },
        },
      },

      legend: {
        top: 2,
        left: 0,

        icon: "roundRect",

        itemWidth: 10,
        itemHeight: 10,
        itemGap: 18,

        textStyle: {
          color: "#8e9ac0",
          fontSize: 12,
          fontWeight: 600,
        },
      },

      xAxis: {
        type: "category",
        boundaryGap: false,
        data: hours,

        axisLine: {
          lineStyle: {
            color:
              "rgba(148,163,255,0.16)",
          },
        },

        axisTick: {
          show: false,
        },

        axisLabel: {
          color: "#8e9ac0",
          fontSize: 11,
          margin: 12,
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
            color:
              "rgba(148,163,255,0.075)",
            type: "dashed",
          },
        },
      },

      series: series.map((item) => ({
        name: item.name,
        type: "line",

        smooth: 0.45,

        showSymbol: false,

        data: item.data,

        lineStyle: {
          width:
            item.name === "INFO"
              ? 3
              : 2,

          color: item.color,
        },

        itemStyle: {
          color: item.color,
        },

        emphasis: {
          focus: "series",
        },

        areaStyle:
          item.name === "INFO"
            ? {
                color: {
                  type: "linear",
                  x: 0,
                  y: 0,
                  x2: 0,
                  y2: 1,

                  colorStops: [
                    {
                      offset: 0,
                      color:
                        "rgba(91,140,255,0.30)",
                    },
                    {
                      offset: 0.55,
                      color:
                        "rgba(91,140,255,0.10)",
                    },
                    {
                      offset: 1,
                      color:
                        "rgba(91,140,255,0)",
                    },
                  ],
                },
              }
            : undefined,
      })),
    }),
    []
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
            Log volume over time
          </p>
        </div>

        <label className="select-field compact">
          <span className="sr-only">
            Chart time range
          </span>

          <select defaultValue="24h">
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
      </div>

      <div className="chart-area">
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
      </div>
    </section>
  );
}

/* ------------------------------------------------------------------ */
/* Level Distribution                                                 */
/* ------------------------------------------------------------------ */

function LevelDistribution({ logs }) {
  const total = logs.length;

  const rows = LEVELS.map((level) => {
    const count = logs.filter(
      (log) => log.level === level
    ).length;

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
              {total.toLocaleString()}
            </strong>

            <span>Matching logs</span>
          </div>
        </div>

        <ul className="level-list">
          {rows.map((row) => (
            <li key={row.level}>
              <div className="level-list-top">
                <LevelBadge
                  level={row.level}
                />

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

/* ------------------------------------------------------------------ */
/* Log Table                                                          */
/* ------------------------------------------------------------------ */

function LogTable({ logs }) {
  return (
    <section className="glass-panel logs-panel">
      <PanelHeader
        label="Log results"
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
            {logs.map((log) => (
              <tr key={log.id}>
                <td className="cell-time">
                  {log.timestamp}
                </td>

                <td>
                  <LevelBadge
                    level={log.level}
                  />
                </td>

                <td>
                  <span className="service-badge">
                    {log.service}
                  </span>
                </td>

                <td className="cell-message">
                  {log.message}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {logs.length === 0 && (
          <div className="empty-state">
            <Icon name="search" size={28} />

            <strong>
              No logs match the current filters
            </strong>

            <span>
              Try a different keyword or clear
              the filters.
            </span>
          </div>
        )}
      </div>
    </section>
  );
}

/* ------------------------------------------------------------------ */
/* Service Health                                                     */
/* ------------------------------------------------------------------ */

function ServiceHealth() {
  return (
    <section className="glass-panel">
      <PanelHeader
        label="Infrastructure"
        title="Service Health"
      >
        <span className="pill pill-green">
          <i
            className="status-dot"
            aria-hidden="true"
          />
          Healthy
        </span>
      </PanelHeader>

      <ul className="service-list">
        {SERVICE_HEALTH.map((service) => (
          <li key={service.name}>
            <span className="service-name">
              <i
                className="status-dot"
                aria-hidden="true"
              />

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

/* ------------------------------------------------------------------ */
/* Live Tail                                                          */
/* ------------------------------------------------------------------ */

function LiveTailPreview({ onOpen }) {
  return (
    <section className="glass-panel live-panel">
      <PanelHeader
        label="Real-time"
        title="Live Tail"
      >
        <span className="pill pill-green">
          <i
            className="status-dot pulse"
            aria-hidden="true"
          />
          Connected
        </span>
      </PanelHeader>

      <div
        className="terminal"
        role="log"
        aria-label="Live log preview"
      >
        {LIVE_PREVIEW.map((entry) => (
          <div
            className="terminal-row"
            key={entry.time}
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
        ))}
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

/* ------------------------------------------------------------------ */
/* Main App                                                           */
/* ------------------------------------------------------------------ */

function App() {
  const [search, setSearch] = useState("");
  const [level, setLevel] = useState("ALL");
  const [service, setService] = useState("ALL");
  const [activePage, setActivePage] =
    useState("Dashboard");

  const searchRef = useRef(null);

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

  const services = useMemo(
    () => [
      "ALL",
      ...new Set(
        mockLogs.map((log) => log.service)
      ),
    ],
    []
  );

  const filteredLogs = useMemo(() => {
    const query = search
      .trim()
      .toLowerCase();

    return mockLogs.filter((log) => {
      const searchable =
        `${log.message} ${log.service} ${log.level}`.toLowerCase();

      return (
        searchable.includes(query) &&
        (level === "ALL" ||
          log.level === level) &&
        (service === "ALL" ||
          log.service === service)
      );
    });
  }, [search, level, service]);

  const stats = useMemo(
    () => ({
      total: filteredLogs.length,

      errors: filteredLogs.filter(
        (log) => log.level === "ERROR"
      ).length,

      warnings: filteredLogs.filter(
        (log) => log.level === "WARN"
      ).length,

      services: new Set(
        filteredLogs.map(
          (log) => log.service
        )
      ).size,
    }),
    [filteredLogs]
  );

  function clearFilters() {
    setSearch("");
    setLevel("ALL");
    setService("ALL");

    requestAnimationFrame(() => {
      searchRef.current?.focus();
    });
  }

  return (
    <div className="app-shell">
      <Sidebar
        activePage={activePage}
        onNavigate={setActivePage}
      />

      <main className="main-content">
        <Topbar title={activePage} />

        <section
          className="metrics-grid"
          aria-label="Key metrics"
        >
          <MetricCard
            icon="logs"
            label="TOTAL LOGS"
            value={stats.total.toLocaleString()}
            trend="12.4%"
            trendUp
            tone="blue"
          />

          <MetricCard
            icon="errorCircle"
            label="ERRORS"
            value={stats.errors.toLocaleString()}
            trend="5.1%"
            trendUp={false}
            tone="red"
          />

          <MetricCard
            icon="warning"
            label="WARNINGS"
            value={stats.warnings.toLocaleString()}
            trend="2.3%"
            trendUp={false}
            tone="amber"
          />

          <MetricCard
            icon="checkCircle"
            label="ACTIVE SERVICES"
            value={stats.services.toLocaleString()}
            trend="100%"
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
          onClear={clearFilters}
          searchRef={searchRef}
        />

        <section className="analytics-grid">
          <LogActivityChart />

          <LevelDistribution
            logs={filteredLogs}
          />
        </section>

        <section className="lower-grid">
          <LogTable logs={filteredLogs} />

          <div className="right-column">
            <ServiceHealth />

            <LiveTailPreview
              onOpen={() =>
                setActivePage("Live Tail")
              }
            />
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;