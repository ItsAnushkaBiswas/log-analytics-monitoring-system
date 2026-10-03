# Log Search API Contract

## Endpoint

GET /api/logs/search

## Query Parameters

| Parameter | Required | Description |
|---|---|---|
| query | No | Search text |
| level | No | Log level such as INFO, WARN, ERROR |
| service | No | Service name |
| startTime | No | Start time for log search |
| endTime | No | End time for log search |
| page | No | Page number |
| size | No | Number of results per page |

## Example Request

GET /api/logs/search?query=database&level=ERROR&page=0&size=10

## Example Response

{
"results": [],
"total": 0,
"page": 0,
"size": 10
}

## Response Fields

- results: List of matching log records
- total: Total number of matching records
- page: Current page number
- size: Number of records requested per page

## Notes

The final request and response fields must be aligned with the
Log Message schema and Lucene search interface provided by Member 1.