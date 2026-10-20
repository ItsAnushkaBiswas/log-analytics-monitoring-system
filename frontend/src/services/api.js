const API_BASE_URL = "http://localhost:8080";

async function fetchJson(url) {
  let response;

  try {
    response = await fetch(url);
  } catch (error) {
    throw new Error(
        `Backend connection failed. Check that Java backend is running at ${API_BASE_URL}.`
    );
  }

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status} ${response.statusText}`);
  }

  return response.json();
}

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

  params.set("page", String(page));
  params.set("size", String(size));

  return fetchJson(
      `${API_BASE_URL}/api/logs/search?${params.toString()}`
  );
}

export async function getLogCount() {
  return fetchJson(
      `${API_BASE_URL}/api/logs/aggregation/count`
  );
}

export async function getLogHistogram(interval = "PT1M") {
  return fetchJson(
      `${API_BASE_URL}/api/logs/aggregation/histogram?interval=${encodeURIComponent(interval)}`
  );
}

export async function getLogCountBy(field = "LEVEL") {
  return fetchJson(
      `${API_BASE_URL}/api/logs/aggregation/count-by?field=${encodeURIComponent(field)}`
  );
}