# Belajar Quarkus – Berita Acara Backend

Backend for managing Word templates and generating Berita Acara documents (Quarkus 3, Java 21).

## Run locally (dev)
```bash
./mvnw compile quarkus:dev
```
- Uses Quarkus Dev Services PostgreSQL (ephemeral), schema `drop-and-create`, seeds users from `import.sql`.
- Default template path: `D:/KULIAH/storage/ba-templates` (override with `TEMPLATE_UPLOAD_PATH`).

## Build / run
```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```
Uber-jar: `./mvnw package -Dquarkus.package.jar.type=uber-jar`.

## Profiles & environment
- Dev (`%dev`): Dev Services DB, `drop-and-create`, loads `import.sql`.
- Prod (`%prod`): Supply DB via env vars `QUARKUS_DATASOURCE_JDBC_URL`, `QUARKUS_DATASOURCE_USERNAME`, `QUARKUS_DATASOURCE_PASSWORD`. Schema `update`; import disabled. Template path via `TEMPLATE_UPLOAD_PATH`. History files stored at `HISTORY_STORAGE_PATH`.

## Health & ops
- SmallRye Health enabled: `/q/health`, `/q/health/live`, `/q/health/ready`.
- Logs: default INFO in prod (adjust via `QUARKUS_LOG_LEVEL` if needed).

## Endpoints (summary)
- Auth: `/auth/register`, `/auth/me`.
- Templates (admin): `/api/admin/templates/...` for upload/scan, define/save, update, status toggle, fetch.
- Generation/history (auth): `/berita-acara/templates`, `/berita-acara/templates/{id}/form-structure`, `/berita-acara/generate-dynamic`, `/berita-acara/history`, `/berita-acara/history/{id}/file`.
