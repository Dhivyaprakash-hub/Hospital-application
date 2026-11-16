# Careway — Consultation Booking

[![Verify](https://github.com/Dhivyaprakash-hub/Hospital-application/actions/workflows/verify.yml/badge.svg?branch=main)](https://github.com/Dhivyaprakash-hub/Hospital-application/actions/workflows/verify.yml)

A fresh rebuild of the archived consultation project as a privacy-conscious appointment scheduling demo. It uses a React 19 + Vite frontend and a Java 21 + Spring Boot 3 REST API with Flyway migrations, sample doctors, filtered specialist search, date/slot availability, booking references, and a PostgreSQL option.

The old packaged app contained a hard-coded database credential and a token authentication bypass. Those archives and their unsafe account/token implementation were removed from the working tree. The rebuilt app deliberately collects no patient name, email, health details, or payment data.

## Run locally

Use Node.js 22.12+ for the frontend and Java 21 for the API. From `backend/`, start the API with the included Maven wrapper.

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```sh
bash ./mvnw spring-boot:run
```

Then start the frontend from `frontend/`:

```sh
npm ci
npm run dev
```

Open the Vite URL (usually `http://localhost:5173`). The API uses an in-memory H2 database seeded with fictional doctors and listens on port 8080. API health is `/actuator/health`.

### Walk through a booking

Open a fictional specialist's appointment times, choose a date and time, and reserve it. Copy the booking reference into the lookup form. The reserved time will be unavailable when you open the same specialist and date again.

![Careway's local booking dialog with a fictional specialist, a date field, and selectable times](docs/images/booking.webp)

If a second booking reserves the time before you submit, the selected slot disappears and you can choose another time.

![The real Careway conflict message shown after another request reserved the selected time](docs/images/booking-conflict.jpg)

## Verify the booking behavior

From `backend/`, run `.\mvnw.cmd test` in PowerShell or `bash ./mvnw test` on macOS/Linux. The tests default to isolated H2. The concurrency test submits two HTTP requests for the same slot and checks for one `201 Created`, one `409 Conflict`, and one committed appointment. The second test checks that inactive doctors are unavailable on read and write paths.

GitHub Actions also runs the suite against a fresh PostgreSQL database. To exercise PostgreSQL locally, set `CAREWAY_TEST_DB_URL`, `CAREWAY_TEST_DB_USERNAME`, and `CAREWAY_TEST_DB_PASSWORD` for a disposable database before running the Maven test command. The suite applies Flyway migrations and writes test rows.

## API

```mermaid
flowchart LR
    UI[React booking screen] -->|Read availability| API[Spring REST API]
    UI -->|Reserve a listed time| API
    API --> S[Transactional booking service]
    S -->|Lock active doctor row| D[(Database)]
    S -->|Check and insert slot| D
    D -->|Unique doctor, date, time| S
    S -->|Slot reserved| Created[201 Created]
    S -->|Slot already taken| Conflict[409 Conflict]
```

- `GET /api/v1/doctors?query=&specialty=` — filter the sample directory
- `GET /api/v1/doctors/{id}/slots?date=YYYY-MM-DD` — list time-slot availability
- `POST /api/v1/appointments` — reserve an available doctor/time
- `GET /api/v1/appointments/{reference}` — retrieve a booking receipt

A booking requires only a doctor ID, a date, and a listed time. The service checks availability in a transaction while locking the doctor row; a database unique constraint protects each doctor/date/time slot. The returned UUID reference can be used to look up the appointment.

## PostgreSQL and deployment

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for PostgreSQL. Set `CORS_ORIGIN` to the exact deployed frontend origin. Flyway owns schema changes; Hibernate validates the schema. The frontend API base may be set with `VITE_API_BASE` at build time.

## Boundaries

This is a portfolio demo with fictional listings and no patient data. It is not a hospital, healthcare provider, medical record system, or source of medical advice. Do not use it for actual patient scheduling. A real healthcare deployment needs identity and authorization, private data handling, audit trails, consent and retention rules, abuse protection, and a security/compliance review. The appointment endpoints are intentionally unauthenticated for this data-free demo and should not be exposed as a real service.

## License

The original Careway application code and documentation are available under the [MIT License](LICENSE). Third-party dependencies retain their own terms. The license does not grant rights to any separate legacy application or third-party assets.
