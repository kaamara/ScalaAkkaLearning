# scala-docker-app

Aplikacja webowa z trzech kontenerów: backend w Scali (Cask), baza PostgreSQL
i statyczny frontend na nginx.

nginx pełni dwie role: serwuje pliki statyczne i jest reverse proxy dla
backendu pod `/api/`. Dzięki temu przeglądarka widzi jeden origin, nie ma
żądań cross-origin, a backend nie zwraca żadnych nagłówków `Access-Control-*`.
Port backendu celowo nie jest publikowany na hoście — jedyne wejście prowadzi
przez nginx.

## Uruchomienie

Hasło do bazy nie jest w repozytorium. Najpierw skopiuj wzorzec:

```bash
cp .env.example .env     # i podmień POSTGRES_PASSWORD
docker compose up -d --build
```

- aplikacja: http://localhost:8080
- API przez proxy: http://localhost:8080/api/messages

```bash
docker compose down      # dane w bazie zostają (wolumen postgres-data)
docker compose down -v   # kasuje też wolumen
```

## Endpointy

| Metoda | Ścieżka (przez proxy) | Opis |
|---|---|---|
| `GET` | `/api/` | Sprawdzenie, że backend żyje |
| `POST` | `/api/add` | Zapis wiadomości; body to zwykły tekst, max 500 znaków |
| `GET` | `/api/messages` | Pięć ostatnich wiadomości |

## Konfiguracja

Backend nie ma wartości domyślnych dla `DB_URL`, `DB_USER` i `DB_PASSWORD` —
przy braku którejkolwiek nie wstaje. To celowe: fallback w rodzaju
`getOrElse("password")` sprawia, że źle skonfigurowany kontener działa na
domyślnym haśle i nikt tego nie zauważa.

Poświadczeń do bazy się nie hashuje — sterownik JDBC musi wysłać je dosłownie.
Chroni się je zarządzaniem sekretem: `.env` poza repozytorium, dalej
`docker secret` montowany jako plik, docelowo Key Vault.

## Stack

Scala 2.13 + Cask, HikariCP, PostgreSQL 15, nginx, Docker Compose.
