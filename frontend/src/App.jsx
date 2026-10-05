import { useState } from "react";
import mockLogs from "./data/mockLogs";
import "./App.css";

function App() {
  const [search, setSearch] = useState("");
  const [level, setLevel] = useState("ALL");
  const [service, setService] = useState("ALL");

  const services = [
    "ALL",
    ...new Set(mockLogs.map((log) => log.service)),
  ];

  const filteredLogs = mockLogs.filter((log) => {
    const matchesSearch =
      log.message.toLowerCase().includes(search.toLowerCase()) ||
      log.service.toLowerCase().includes(search.toLowerCase());

    const matchesLevel =
      level === "ALL" || log.level === level;

    const matchesService =
      service === "ALL" || log.service === service;

    return matchesSearch && matchesLevel && matchesService;
  });

  return (
    <div className="app">
      <header className="header">
        <div>
          <h1>Log Stream</h1>
          <p>Distributed Log Analytics & Monitoring</p>
        </div>

        <div className="status">
          <span className="status-dot"></span>
          System Online
        </div>
      </header>

      <main className="dashboard">
        <section className="search-section">
          <input
            type="text"
            placeholder="Search logs..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />

          <select
            value={level}
            onChange={(e) => setLevel(e.target.value)}
          >
            <option value="ALL">All Levels</option>
            <option value="INFO">INFO</option>
            <option value="WARN">WARN</option>
            <option value="ERROR">ERROR</option>
            <option value="DEBUG">DEBUG</option>
          </select>

          <select
            value={service}
            onChange={(e) => setService(e.target.value)}
          >
            {services.map((item) => (
              <option key={item} value={item}>
                {item === "ALL" ? "All Services" : item}
              </option>
            ))}
          </select>

          <button
            onClick={() => {
              setSearch("");
              setLevel("ALL");
              setService("ALL");
            }}
          >
            Clear
          </button>
        </section>

        <section className="summary">
          <div className="summary-card">
            <span>Total Logs</span>
            <strong>{filteredLogs.length}</strong>
          </div>

          <div className="summary-card">
            <span>Errors</span>
            <strong>
              {filteredLogs.filter((log) => log.level === "ERROR").length}
            </strong>
          </div>

          <div className="summary-card">
            <span>Warnings</span>
            <strong>
              {filteredLogs.filter((log) => log.level === "WARN").length}
            </strong>
          </div>
        </section>

        <section className="logs-section">
          <div className="section-title">
            <h2>Log Results</h2>
            <span>{filteredLogs.length} records</span>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Level</th>
                  <th>Service</th>
                  <th>Message</th>
                </tr>
              </thead>

              <tbody>
                {filteredLogs.map((log) => (
                  <tr key={log.id}>
                    <td>{log.timestamp}</td>

                    <td>
                      <span className={`level ${log.level.toLowerCase()}`}>
                        {log.level}
                      </span>
                    </td>

                    <td>{log.service}</td>

                    <td>{log.message}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            {filteredLogs.length === 0 && (
              <div className="empty-state">
                No logs match the current filters.
              </div>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;