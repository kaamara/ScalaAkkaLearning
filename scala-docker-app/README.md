# scala-docker-app

Aplikacja webowa z trzech kontenerów: backend w Scali (Cask), baza PostgreSQL
i statyczny frontend na nginx. Frontend wysyła wiadomości przez `POST /add`,
backend zapisuje je w bazie i udostępnia ostatnie wpisy przez `GET /messages`.

## Uruchomienie

```bash
docker compose up -d --build
```

- frontend: http://localhost:8080
- backend: http://localhost:8081

```bash
docker compose down
```

## Stack

Scala 2.13 + Cask, PostgreSQL 15, nginx, Docker Compose.
