const API_BASE_URL = "http://localhost:8081";

export async function searchLogs({
  text = "",
  levels = "",
  services = "",
  from = "",
  to = "",
  page = 0,
  size = 50,
} = {}) {
  const params = new URLSearchParams();

  if (text) params.set("text", text);
  if (levels) params.set("levels", levels);
  if (services) params.set("services", services);
  if (from) params.set("from", from);
  if (to) params.set("to", to);

  params.set("page", page);
  params.set("size", size);

  const response = await fetch(
    `${API_BASE_URL}/api/logs/search?${params.toString()}`
  );

  if (!response.ok) {
    throw new Error(`Search API failed: ${response.status}`);
  }

  return response.json();
}

export async function getLogCount() {
  const response = await fetch(
    `${API_BASE_URL}/api/logs/aggregation/count`
  );

  if (!response.ok) {
    throw new Error(`Count API failed: ${response.status}`);
  }

  return response.json();
}

export async function getLogHistogram(interval = "PT1M") {
  const response = await fetch(
    `${API_BASE_URL}/api/logs/aggregation/histogram?interval=${encodeURIComponent(interval)}`
  );

  if (!response.ok) {
    throw new Error(`Histogram API failed: ${response.status}`);
  }

  return response.json();
}

export async function getLogCountBy(field = "LEVEL") {
  const response = await fetch(
    `${API_BASE_URL}/api/logs/aggregation/count-by?field=${field}`
  );

  if (!response.ok) {
    throw new Error(`Count-by API failed: ${response.status}`);
  }

  return response.json();
}