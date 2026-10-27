# Two Spring Boot services with Postgres

Service A (`auth-api`) registers users, issues a JWT, and exposes a protected process endpoint. That endpoint calls Service B (`data-api`), which reverses the text and uppercases it. Service A stores the input and output in Postgres.

`hello` becomes `OLLEH`.

## Layout

```
auth-api/           Spring Boot: auth, JWT, JPA, processing log
data-api/           Spring Boot: internal transform endpoint
docker-compose.yml  postgres, auth-api, data-api on one network
```

Passwords are stored as BCrypt hashes. The JWT secret and internal token come from environment variables and are not written to logs.

## Prerequisites

- Docker with Compose v2

Local `mvn package` also needs JDK 21 and Maven 3.9+. `docker compose up --build` compiles inside the images, so a local JDK is optional.

## Run

From the repository root:

```bash
mvn -f auth-api/pom.xml clean package -DskipTests
mvn -f data-api/pom.xml clean package -DskipTests
docker compose up -d --build --wait
```

`--wait` blocks until Postgres and both APIs are healthy. The first build downloads Maven dependencies and can take a few minutes.

Ports:

| Service   | URL |
|-----------|-----|
| auth-api  | http://localhost:8080 |
| data-api  | http://localhost:8081 |
| Postgres  | localhost:5432 (`app` / `app`, database `appdb`) |

Service A calls Service B at `http://data-api:8081` on the Compose network.

## Try it

Register (`201`):

```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"a@a.com","password":"pass"}'
```

Login (`200` and a `token`):

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"a@a.com","password":"pass"}'
```

Process (`Authorization` required). Replace `<token>` with the login token:

```bash
curl -s -X POST http://localhost:8080/api/process \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"text":"hello"}'
```

Expected body:

```json
{"result":"OLLEH"}
```

The same call writes a `processing_log` row (`user_id`, `input_text`, `output_text`, `created_at`).

```bash
docker compose exec postgres \
  psql -U app -d appdb \
  -c "SELECT input_text, output_text, created_at FROM processing_log;"
```

Service B rejects calls that omit `X-Internal-Token` or send the wrong value (`403`):

```bash
curl -i -X POST http://localhost:8081/api/transform \
  -H "Content-Type: application/json" \
  -d '{"text":"hello"}'
```

A direct call with the dev token succeeds:

```bash
curl -s -X POST http://localhost:8081/api/transform \
  -H "Content-Type: application/json" \
  -H "X-Internal-Token: dev-internal-token" \
  -d '{"text":"hello"}'
```

One-shot process call after login:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"a@a.com","password":"pass"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["token"])')

curl -s -X POST http://localhost:8080/api/process \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"text":"hello"}'
```

## Configuration

Set these on `auth-api`:

| Variable | Purpose |
|----------|---------|
| `POSTGRES_URL` | JDBC URL, for example `jdbc:postgresql://postgres:5432/appdb` |
| `POSTGRES_USER` | Database user |
| `POSTGRES_PASSWORD` | Database password |
| `JWT_SECRET` | HMAC secret, at least 32 bytes |
| `INTERNAL_TOKEN` | Shared secret sent as `X-Internal-Token` |
| `DATA_API_URL` | Service B base URL. Compose sets `http://data-api:8081` |

Set `INTERNAL_TOKEN` on `data-api` to the same value. The dev values live in `docker-compose.yml`. Change them before using this anywhere else.

Registering the same email again returns `409`. A wrong password returns `401`. `/api/process` without a bearer token returns `401`.

## Schema

Flyway migration `auth-api/src/main/resources/db/migration/V1__init.sql` creates:

- `users` — `id` (UUID), `email` (unique), `password_hash`
- `processing_log` — `id` (UUID), `user_id`, `input_text`, `output_text`, `created_at`

## Stop

```bash
docker compose down
```

`docker compose down -v` also deletes the Postgres volume so you can register `a@a.com` again.
