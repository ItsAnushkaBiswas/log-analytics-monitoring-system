import { useState } from "react";
import mockLogs from "./data/mockLogs";
import SearchBar from "./components/SearchBar";
import FilterPanel from "./components/FilterPanel";
import LogTable from "./components/LogTable";
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
    const text = `${log.message} ${log.service}`.toLowerCase();

    const matchesSearch = text.includes(search.toLowerCase());

    const matchesLevel =
      level === "ALL" || log.level === level;

    const matchesService =
      service === "ALL" || log.service === service;

    return matchesSearch && matchesLevel && matchesService;
  });

  function clearFilters() {
    setSearch("");
    setLevel("ALL");
    setService("ALL");
  }

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
          <SearchBar
            search={search}
            setSearch={setSearch}
          />

          <FilterPanel
            level={level}
            setLevel={setLevel}
            service={service}
            setService={setService}
            services={services}
            onClear={clearFilters}
          />
        </section>

        <section className="summary">
          <div className="summary-card">
            <span>Total Logs</span>
            <strong>{filteredLogs.length}</strong>
          </div>

          <div className="summary-card">
            <span>Errors</span>
            <strong>
              {filteredLogs.filter(
                (log) => log.level === "ERROR"
              ).length}
            </strong>
          </div>

          <div className="summary-card">
            <span>Warnings</span>
            <strong>
              {filteredLogs.filter(
                (log) => log.level === "WARN"
              ).length}
            </strong>
          </div>
        </section>

        <LogTable logs={filteredLogs} />

      </main>
    </div>
  );
}

export default App;