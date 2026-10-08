function LogTable({ logs }) {
  return (
    <section className="logs-section">
      <div className="section-title">
        <h2>Log Results</h2>
        <span>{logs.length} records</span>
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
            {logs.map((log) => (
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

        {logs.length === 0 && (
          <div className="empty-state">
            No logs match the current filters.
          </div>
        )}
      </div>
    </section>
  );
}

export default LogTable;