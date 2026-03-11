# FX Deals Warehouse - ProgressSoft Assignment

A clean, production-style Spring Boot service that imports FX deal records from CSV into PostgreSQL for analytics workloads.

## ✅ What is already implemented

- CSV upload endpoint for bulk deal ingestion.
- Row-level validation (format, required fields, amount, ISO currency shape).
- Duplicate protection using **Deal Unique Id** as the persisted key.
- Fault-tolerant import flow: valid rows are saved even if some rows fail.
- Header-row detection (standard assignment-style header is ignored when present).
- PostgreSQL-backed persistence (plus H2 for tests).
- Docker Compose setup for one-command local environment.
- Unit + controller tests, with JaCoCo report generation.
- Global exception handling with consistent JSON error responses.
- Makefile shortcuts for common developer tasks.

## Stack

- Java 17
- Spring Boot 3.3
- Spring Web, Spring Data JPA, Bean Validation
- PostgreSQL
- JUnit 5, Mockito, MockMvc

## CSV input contract

Each deal row must have exactly 5 columns in this order:

1. Deal Unique Id
2. From Currency ISO Code (ordering currency)
3. To Currency ISO Code
4. Deal Timestamp (ISO-8601 with offset, e.g. `2024-06-01T09:00:00+00:00`)
5. Deal Amount in ordering currency

Example file: `sample/deals.csv`

> Tip: a first-row header matching the above assignment labels is accepted and skipped.

## Run locally

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

- Field name: `file`

Example:

```bash
curl -X POST http://localhost:8080/api/deals/import \
  -F "file=@sample/deals.csv"
```

### Success response shape

```json
{
  "totalRows": 3,
  "importedRows": 3,
  "duplicateRows": 0,
  "invalidRows": 0,
  "errors": []
}
```

## Duplicate handling behavior

- `deal_unique_id` is the primary key.
- Existing IDs are skipped and counted under `duplicateRows`.

## Test coverage

Run:

```bash
mvn clean test
```

Coverage report:

`target/site/jacoco/index.html`

## Submission readiness checklist

- [x] Functional requirements implemented.
- [x] Defensive validation and duplicate handling in place.
- [x] Containerized runtime provided.
- [x] Tests included.
- [ ] CI-based build verification from a network-enabled environment.
- [ ] Optional polish: add API contract docs (OpenAPI) and import metrics endpoint.
