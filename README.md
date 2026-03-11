# FX Deals Warehouse - ProgressSoft Assignment

Spring Boot service that imports FX deal records from CSV into PostgreSQL for analytics.

## Implemented Requirements

- Accept deal details and persist to database.
- Validate row structure and field formats.
- Prevent duplicate imports based on **Deal Unique Id**.
- Process row-by-row so valid rows are saved even when some rows fail.
- Actual DB support with PostgreSQL.
- Docker Compose deployment.
- Maven project with tests and coverage report.
- Structured exception handling and logging.
- Makefile for common commands.

## Tech Stack

- Java 17
- Spring Boot 3.3
- Spring Web + Spring Data JPA + Validation
- PostgreSQL
- JUnit 5 + Mockito + MockMvc

## Input Format (CSV)

Each row must contain exactly 5 columns in order:

1. Deal Unique Id
2. From Currency ISO Code (ordering currency)
3. To Currency ISO Code
4. Deal Timestamp (ISO-8601 with offset, e.g. `2024-06-01T09:00:00+00:00`)
5. Deal Amount in ordering currency

Example available at: `sample/deals.csv`

## Run Locally

```bash
make test
make run
```

## Run with Docker Compose

```bash
make docker-up
```

Service URL: `http://localhost:8080`

Stop containers:

```bash
make docker-down
```

## API

### Import deals

`POST /api/deals/import` (multipart/form-data)

- Form field name: `file`

Example:

```bash
curl -X POST http://localhost:8080/api/deals/import \
  -F "file=@sample/deals.csv"
```

Sample response:

```json
{
  "totalRows": 3,
  "importedRows": 3,
  "duplicateRows": 0,
  "invalidRows": 0,
  "errors": []
}
```

## Notes on Duplicate Handling

- `deal_unique_id` is the primary key.
- Existing IDs are skipped and counted as duplicates.

## Test Coverage

Run:

```bash
mvn clean test
```

Coverage HTML report generated at:

`target/site/jacoco/index.html`
