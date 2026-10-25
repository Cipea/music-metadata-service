# Music Metadata Service

A Spring Boot REST API for artist and track metadata. PostgreSQL stores the
catalogue, and Redis caches read responses.

## Overview

The service exposes secure REST endpoints for retrieving, creating, and updating
artist and track data, including alias management and a rotating Artist of the
Day selection. It is designed for local development with Docker Compose and
supports production-style observability through Spring Boot Actuator and Prometheus.

## Technology stack

The latest project configuration in `pom.xml` includes:

- Java 25
- Spring Boot 4.1.1
- Spring Data JPA, Validation, Web MVC, Cache, Redis, Security, and Actuator
- PostgreSQL for persistence and Flyway for schema/seed migrations
- OAuth2 Resource Server for JWT bearer authentication with Auth0
- SpringDoc OpenAPI UI for Swagger and OpenAPI docs
- Redis for caching and cache invalidation
- Micrometer Prometheus registry for metrics export
- Lombok for boilerplate reduction
- Testcontainers for integration testing

## Run locally with Docker Compose

Requirements: Docker with the Compose plugin.

From this directory, build and start the API, PostgreSQL, Redis, and Prometheus:

```sh
docker compose up --build
```

The API is available at `http://localhost:8080`. Compose starts PostgreSQL on
port `5432`, Redis on port `6379`, and Prometheus on port `9090`. Flyway creates
the schema and loads sample artists and tracks. To stop the services:

```sh
docker compose down
```

To also remove the local PostgreSQL data volume and reset the database:

```sh
docker compose down --volumes
```

> Removing the volume deletes the local database data.

## Monitoring

Spring Boot Actuator exposes the `health`, `info`, and `prometheus` web endpoints.
The Prometheus metrics endpoint is available at
`http://localhost:8080/actuator/prometheus`, and the Prometheus UI is available
at `http://localhost:9090`. Prometheus scrapes the API every five seconds using
the configuration in `src/main/resources/prometheus.yml`.

If you change the application configuration or dependencies, rebuild and restart
the app container to apply the changes:

```sh
docker compose up -d --build app
```

## Seed data

Flyway creates the schema from `src/main/resources/db/migration/V1__init_schema_creation.sql`
and loads the sample catalogue from these versioned seed scripts, in order:

- `src/main/resources/db/seed/V2__insert_sample_data.sql` adds The Midnight and Luna & The Tides, one alias, and three tracks.
- `src/main/resources/db/seed/V3__insert_more_sample_data.sql` adds eight more artists, four aliases, and twelve tracks.

The local profile and Docker Compose configure Flyway to scan both
`classpath:db/migration` and `classpath:db/seed`. Flyway runs each script once
when the application starts; edits to an already-applied script are not
re-executed. To apply a changed seed set to a fresh local database, stop Compose
and remove its PostgreSQL volume with `docker compose down --volumes`, then start
it again with `docker compose up --build`.

## Authentication

The API endpoints require a valid JWT bearer token issued by the Auth0 issuer
configured for this service. Obtain a token with access to the configured API
from your development Auth0 setup, then set it in your shell:

```sh
export TOKEN='your-jwt-access-token'
```

Swagger UI, the OpenAPI document, and the exposed Actuator endpoints
(`health`, `info`, and `prometheus`) do not require authentication:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Actuator health: `http://localhost:8080/actuator/health`

Other requests without a valid token return `401 Unauthorized`.

## Test APIs with Postman

Import `postman/Metadata Service API.postman_collection.json` into Postman. The
collection contains requests for adding a track or artist alias, updating an
artist name, and retrieving artist tracks or the Artist of the Day. Requests
target the local API at `http://localhost:8080` and use the seeded artist ID
`11111111-1111-1111-1111-111111111111`.

Before sending requests, set these collection variables in Postman:

| Variable | Value |
| --- | --- |
| `auth0_client_id` | Client ID for your Auth0 machine-to-machine application |
| `auth0_client_secret` | Client secret for that application |
| `auth0_audience` | Identifier of the Auth0 API configured for this service |

The collection's pre-request script uses those values to request a client-
credentials access token from the configured Auth0 tenant. It stores the token
in `current_access_token`, which the requests send as a bearer token. Keep the
client secret private; do not commit or share a Postman environment or collection
export containing real credentials. The collection's Auth0 token URL is set to
the development tenant used by this service.

## Manually test the API

The seed scripts load these artists:

| Artist | ID |
| --- | --- |
| The Midnight | `11111111-1111-1111-1111-111111111111` |
| Luna & The Tides | `22222222-2222-2222-2222-222222222222` |
| Northstar | `77777777-7777-7777-7777-777777777777` |
| Maya Rivers | `88888888-8888-8888-8888-888888888888` |
| Paper Lanterns | `99999999-9999-9999-9999-999999999999` |
| Elias Vale | `aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa` |
| Cinder Coast | `bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb` |
| Nia Sol | `cccccccc-cccc-cccc-cccc-cccccccccccc` |
| Juniper Vale | `dddddddd-dddd-dddd-dddd-dddddddddddd` |
| Theo Park | `eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee` |

For convenience, define the first artist ID:

```sh
export ARTIST_ID='11111111-1111-1111-1111-111111111111'
```

### Get an artist's tracks

```sh
curl -i \
  -H "Authorization: Bearer $TOKEN" \
  'http://localhost:8080/api/v1/artist/11111111-1111-1111-1111-111111111111/tracks?page=0&size=20'
```

Try `page=1` or another `size` to request a different cached page.

### Add a track

```sh
curl -i -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "New Song",
    "durationSeconds": 215,
    "genre": "Synthwave",
    "releaseDate": "2026-10-23"
  }' \
  "http://localhost:8080/api/v1/artist/$ARTIST_ID/tracks"
```

Expect `201 Created`. Fetch the tracks again to confirm the new track is present
and the cached response was refreshed.

### Add an alias

```sh
curl -i -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"aliasName":"The Midnight Project"}' \
  "http://localhost:8080/api/v1/artist/$ARTIST_ID/aliases"
```

The response includes the artist and its aliases. To test duplicate-alias
handling, submit the same request again: the API returns `409 Conflict`.

### Update an artist's primary name

```sh
curl -i -X PATCH \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"newPrimaryName":"The Midnight Updated"}' \
  "http://localhost:8080/api/v1/artist/$ARTIST_ID/name"
```

The previous primary name is retained as an alias.

### Get the Artist of the Day

```sh
curl -i \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/artist/artist-of-the-day
```

The selected artist advances once per UTC day through artists ordered by ID,
then wraps to the beginning of the catalogue. The daily result is cached in
Redis.

### Check Redis

List cache keys from the Redis container:

```sh
docker compose exec redis redis-cli --scan --pattern 'artist-*'
```

Cache entries use a 30-minute TTL. Track cache entries are evicted after track
or artist updates; artist updates also evict the Artist of the Day cache.

## Run tests

The project uses Java 25 and Maven. From this directory:

```sh
bash ./mvnw test
```

The Spring context test uses the configured PostgreSQL and Redis services, so
start them first if they are not already running:

```sh
docker compose up -d postgres redis
```
