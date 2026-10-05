function FilterPanel({
  level,
  setLevel,
  service,
  setService,
  services,
  onClear,
}) {
  return (
    <div className="filter-panel">
      <select value={level} onChange={(e) => setLevel(e.target.value)}>
        <option value="ALL">All Levels</option>
        <option value="INFO">INFO</option>
        <option value="WARN">WARN</option>
        <option value="ERROR">ERROR</option>
        <option value="DEBUG">DEBUG</option>
      </select>

      <select value={service} onChange={(e) => setService(e.target.value)}>
        {services.map((item) => (
          <option key={item} value={item}>
            {item === "ALL" ? "All Services" : item}
          </option>
        ))}
      </select>

      <button onClick={onClear}>Clear</button>
    </div>
  );
}

export default FilterPanel;