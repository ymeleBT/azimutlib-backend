# University Library Management System

Book cataloguing, borrowing, returning, reservations and fines for a private university library in Cameroon.

- **Backend:** Spring Boot (Java 21), MySQL, Flyway migrations, JWT auth
- **Frontend:** Angular, Angular Material, bilingual EN/FR (ngx-translate)

## Project layout

```
backend/    Spring Boot REST API (cm.univ.library)
frontend/   Angular app
```

## 1. Database setup

Create the database and an application user in MySQL:

```sql
CREATE DATABASE library_db CHARACTER SET utf8mb4;
CREATE USER 'library_user'@'localhost' IDENTIFIED BY 'library_pass';
GRANT ALL PRIVILEGES ON library_db.* TO 'library_user'@'localhost';
FLUSH PRIVILEGES;
```

Flyway creates the schema automatically on first backend startup (`backend/src/main/resources/db/migration`).

## 2. Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

Config is in `src/main/resources/application.yml`, overridable via env vars: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `CORS_ALLOWED_ORIGINS`.

- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html

A default admin account is seeded on first boot: **matricule `ADMIN001`, password `ChangeMe123!`** — change it immediately (there is no self-service password change endpoint yet; update it directly via the `/api/users` flow or a DB update in the interim).

## 3. Run the frontend

```bash
cd frontend
npm install
npm start
```

Serves at http://localhost:4200, proxying API calls to `http://localhost:8080/api` (see `src/environments/environment.ts`).

## What's implemented

- JWT auth (matricule + password), role-based access: STUDENT, LECTURER, LIBRARIAN, ADMIN
- Book catalog (browse/search, categories, multi-copy tracking)
- Borrow / return / renew, with per-role policy (loan duration, concurrent-loan limit, renewals) in `library_policy`
- Reservations with automatic hand-off when a copy is returned, and expiry of unclaimed holds
- Overdue fines (auto-calculated on late return), librarian fine settlement
- Email + SMS notifications (SMS gateway is a no-op stub by default — see `notification/SmsGateway.java`, wire up a real Cameroonian aggregator by implementing that interface and setting `SMS_ENABLED=true`)
- Daily scheduled jobs: mark loans overdue, send due-soon reminders, expire unclaimed reservations
- Bilingual UI (English/French)

## What's next (not yet built)

- Reporting/analytics dashboards (most-borrowed titles, overdue lists, fine collection)
- Self-service password change / reset flow
- CSV/bulk import of students and lecturers from the registrar
- Barcode scanner integration at the circulation desk (currently manual entry by design)
- Production deployment setup (Docker Compose, CI)

## Default library policy (seeded, editable in `library_policy` table)

| Role     | Loan duration | Max concurrent loans | Max renewals | Fine/day |
|----------|---------------|----------------------|---------------|----------|
| STUDENT  | 14 days       | 3                     | 1             | 100 XAF  |
| LECTURER | 30 days       | 8                     | 2             | 100 XAF  |
