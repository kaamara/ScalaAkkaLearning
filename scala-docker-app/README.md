# Scala Docker App

## Opis
Aplikacja webowa złożona z trzech kontenerów: backendu w Scali (Cask),
bazy PostgreSQL i statycznego frontendu (nginx). Frontend wysyła
wiadomości do backendu (`POST /add`), a backend zapisuje je w bazie
i udostępnia ostatnie wpisy (`GET /messages`).

## Uruchomienie
```
docker-compose up --build
```

Frontend: http://localhost:8080
Backend: http://localhost:8081

## Stack
- Scala 2.13, Cask
- PostgreSQL 15
- nginx (frontend)
- Docker Compose
