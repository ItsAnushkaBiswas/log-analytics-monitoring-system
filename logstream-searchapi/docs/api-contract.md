
# Log Search API Contract

## Endpoint

GET /api/logs/search

## Query Parameters

| Parameter | Required | Description |
|---|---|---|
| text | No | Text to search in logs |
| levels | No | Set of log levels, e.g. INFO, WARN, ERROR |
| services | No | Set of service names |
| from | No | Start timestamp (ISO-8601) |
| to | No | End timestamp (ISO-8601) |
| page | No | Zero-based page number; default 0 |
| size | No | Results per page; default 50, maximum 500 |

## Example Request

GET /api/logs/search?text=database&levels=ERROR&page=0&size=10

## Example Response

{
"results": [],
"total": 0,
"page": 0,
"size": 10,
"tookMs": 19
}

## Response Fields

- results: List of matching log records
- total: Total number of matching records
- page: Current zero-based page number
- size: Requested page size
- tookMs: Search execution time in milliseconds

## Validation

- page must be 0 or greater
- size must be between 1 and 500
- from must not be after to

Invalid parameters return HTTP 400 Bad Request.

## Notes

The API uses the LogQuery and LogSearchResponse types
implemented by the Search API backend.
