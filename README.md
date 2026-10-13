# Mini Ticket Tracker — Backend

A small Spring Boot REST API backing the Mini Ticket Tracker Angular frontend.
It manages support tickets (create, list, filter, update) with a forward‑only
status workflow and a per‑ticket audit history.

- **Stack:** Java 21, Spring Boot 3.5, Spring Data JPA (Hibernate), PostgreSQL,
  Flyway, Bean Validation, springdoc‑openapi (Swagger UI).
- **Build:** Maven (via the bundled wrapper — no local Maven needed).
- **API base path:** `/api/v1/tickets` · **Default port:** `8090`.

---
## Prerequisites

| Tool | Version | Notes                                                                       |
|---|---|-----------------------------------------------------------------------------|
| JDK | 21 (LTS) | `java -version` should report 21.x                                          |
| PostgreSQL | 14+ (built against 18) | A running server reachable on `localhost:8090`                                |
| Docker | any recent | **Only for running the tests** (Testcontainers). Not needed to run the app. |
| Maven | — | Not required; use the `./mvnw` / `mvnw.cmd` wrapper                         |

---

## 1. Create the database

Flyway creates the **tables**, but the **database** itself must exist first.

```sql
CREATE DATABASE ticket_tracker;
```

(Using pgAdmin: right‑click *Databases → Create → Database*, name it
`ticket_tracker`. Or with the CLI: `createdb -U postgres ticket_tracker`.)

On first start, Flyway runs the migration in `src/main/resources/db/migration`
and creates the `tickets` and `ticket_history` tables plus its own
`flyway_schema_history`. Flyway is idempotent — subsequent starts detect the
schema is up to date and do nothing.

---

## 2. Configuration

Connection settings live in `src/main/resources/application.yaml` and are
**overridable by environment variables**, with sensible local defaults:

| Env var | Default | Meaning |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/ticket_tracker` | JDBC URL |
| `DB_USERNAME` | `postgres` | DB user |
| `DB_PASSWORD` | `postgres` | DB password |

The server port defaults to **8090** (the frontend dev proxy points here). Other
settings of note: `spring.jpa.hibernate.ddl-auto=validate` (Flyway owns the
schema; Hibernate only validates the entities against it) and
`spring.jpa.open-in-view=false`.

---

## 3. Run from source (for the dev team)

The Maven wrapper downloads the right Maven version automatically.

```bash
# macOS / Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The app starts on <http://localhost:8090>. Point it at a different database
without editing files:

```bash
# macOS / Linux
DB_URL=jdbc:postgresql://db-host:5432/ticket_tracker \
DB_USERNAME=app DB_PASSWORD=secret \
./mvnw spring-boot:run
```

---

## 4. Build and run the executable JAR (for deployment)

Spring Boot produces a single self‑contained ("fat") JAR that needs only a JRE
and a reachable PostgreSQL — no Maven, no source.

```bash
# build (skip tests for a faster package; see section 5 to run them)
./mvnw clean package -DskipTests

# run
java -jar target/ticket-tracker-0.0.1-SNAPSHOT.jar
```

Override the database at runtime via environment variables or Spring args — no
rebuild required:

```bash
# via environment variables
DB_URL=jdbc:postgresql://db-host:5432/ticket_tracker \
DB_USERNAME=app DB_PASSWORD=secret \
java -jar target/ticket-tracker-0.0.1-SNAPSHOT.jar

# or via command-line properties
java -jar target/ticket-tracker-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:postgresql://db-host:5432/ticket_tracker \
  --spring.datasource.username=app \
  --spring.datasource.password=secret \
  --server.port=8090
```

---

## 5. Tests

Integration tests run the full stack over a **throwaway PostgreSQL** started by
Testcontainers (with Flyway applied), so they need **Docker running**.

```bash
./mvnw verify
```

Coverage (API via MockMvc + a persistence slice):

- Create → `201`, status forced to `open`, a `created` history row is written.
- Blank title → `400 ValidationError` with `fieldErrors`.
- Stats reflect created and transitioned tickets.
- Illegal status transition (`open → closed`) → `409 InvalidTransition`.
- List filtered by `status`.

---

## 6. API reference

Interactive docs (springdoc‑openapi) once the app is running:

- Swagger UI: <http://localhost:8090/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8090/v3/api-docs>

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/tickets` | Create a ticket (status always starts `open`). `201`. |
| `GET` | `/api/v1/tickets` | List: `page` (0‑based), `size`, `sort` (e.g. `createdAt,desc`), optional `status`, `priority`, `search`. Returns a Spring `Page`. |
| `GET` | `/api/v1/tickets/stats` | Counts: `{open, in_progress, closed, total, low, medium, high}`. |
| `GET` | `/api/v1/tickets/{id}` | One ticket. `404` if missing. |
| `GET` | `/api/v1/tickets/{id}/history` | Audit trail, newest first. |
| `PATCH` | `/api/v1/tickets/{id}` | Partial update. Illegal status jump → `409`. |

**Enums (wire values):** status `open | in_progress | closed`, priority
`low | medium | high`.

**State machine:** forward‑only `open → in_progress → closed`. Same‑status is a
no‑op; skipping or going backwards returns `409`. A closed ticket's status is
locked, but its title/description/priority remain editable.

**Error shape** (all failures):

```json
{ "status": 400, "error": "ValidationError", "message": "…", "fieldErrors": { "title": "…" } }
```

`fieldErrors` is present only for validation failures. Codes used: `NotFound`
(404), `ValidationError` (400), `InvalidTransition` (409), `InternalError` (500).

---

## 7. Project layout

```
src/main/java/com/lodwar/ticket_tracker
  config/      JpaConfig (auditing), WebConfig (enum query-param converters)
  domain/      Ticket, TicketHistory, enums + JPA converters, state machine
  repository/  Spring Data repositories (+ Specifications executor)
  service/     TicketService (business logic, transactions, history), Specifications
  web/         TicketController
  web/dto/     request/response records + mapper
  web/error/   ApiError + @RestControllerAdvice
src/main/resources
  application.yaml
  db/migration/V1__init.sql
```

---

## 8. Decisions & tradeoffs

- **Flyway owns the schema; `ddl-auto=validate`.** Migrations are the single
  source of truth and Hibernate only validates entities against them, so the
  schema never drifts silently. Enum columns are `varchar` with `CHECK`
  constraints rather than native PG enums, which are painful to evolve.
- **Return Spring's native `Page<T>`.** The frontend already adapts the Spring
  page shape, so emitting it directly avoids a custom wrapper. Tradeoff: it
  couples the wire format to Spring Data's serialization; with more time I'd
  expose an explicit `PageResponse` DTO so the contract is framework‑independent.
- **Enum wire values via `@JsonValue`/`@JsonCreator` + a JPA `AttributeConverter`
  + MVC `Converter`s.** All three layers (JSON body, DB, query params) agree on
  the lowercase spelling (`in_progress`). `@JsonValue` alone does **not** cover
  `@RequestParam` binding — hence the explicit MVC converters.
- **State machine lives on the `TicketStatus` enum.** `canTransitionTo(...)` is
  the one place the rule is defined, mirrored by the frontend. The service
  enforces it and raises a `409` (a conflict with current state, not a 400).
- **History is written in the same transaction as the change.** A ticket update
  and its audit row commit or roll back together — no change without its trail.
- **Hand‑written DTO mapping, no Lombok, no MapStruct.** At this size the
  explicit mapper is clearer and keeps the dependency surface small. Entities are
  plain classes (JPA needs mutability + a no‑arg constructor, so records don't
  fit); DTOs are records.
- **Timestamps are `OffsetDateTime` (`TIMESTAMPTZ`)** to match the ISO‑8601
  contract, with a custom auditing `DateTimeProvider` since Spring Data's default
  yields `LocalDateTime`.
- **No authentication** — out of scope for this assignment. In production I'd add
  a JWT/OAuth2 resource server and ownership checks.

## With more time

Product/workflow:

- **Authentication & authorization.** A JWT or cookie‑based session (Spring
  Security resource server) establishing the current principal, then ownership
  and role checks on every mutating endpoint.
- **People on a ticket.** Track `created_by` and `assigned_to` as FKs to a users
  table once a principal exists, surfaced in the list/detail responses.
- **Separation of duties.** Enforce that the person who resolves a ticket is not
  the one who closes it — a domain invariant checked in the service (a `409`/`403`,
  the same pattern as the state machine), mirroring real approval‑workflow
  controls.
- **Re‑opening closed tickets.** Closed is terminal here, but real life needs
  re‑open. Because the rule lives in one place (`TicketStatus.canTransitionTo`),
  adding `closed → in_progress` is a one‑line change — the payoff of centralizing
  the state machine.
- **Re‑open rate as a business metric.** Reopened tickets are a quality signal
  (premature closes, flaky fixes). This can be **derived from the existing audit
  trail** — every transition already writes a `status_changed` row — so the
  analytics need no new write path, just a query over `ticket_history`.

Scalability:

- **Pre‑aggregated stats table.** The current `GET /stats` runs
  `COUNT(*) … GROUP BY`, which is O(rows) on every call — fine here, but it does
  not scale to a Jira‑sized dataset (millions of tickets). The mature pattern is a
  small counter table keyed by status/priority bucket, incremented/decremented
  **in the same transaction** as each create/update (which this design already does
  for history). Stats then becomes an O(1) read. Under concurrency the counter
  updates must be atomic (`UPDATE … SET count = count + 1`) rather than
  read‑modify‑write to avoid lost updates; a materialized view is the lazier
  alternative but goes stale.

Engineering:

- Bulk status transitions (`PATCH /tickets`), optimistic locking (`@Version`),
  request/response logging and metrics, a Dockerfile + compose for one‑command
  local Postgres, and richer OpenAPI annotations.

