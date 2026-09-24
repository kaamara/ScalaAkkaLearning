# scala-docker-app

Aplikacja webowa z trzech kontenerów: backend w Scali (Cask), baza PostgreSQL
i statyczny frontend na nginx.

nginx pełni dwie role: serwuje pliki statyczne i jest reverse proxy dla
backendu pod `/api/`. Dzięki temu przeglądarka widzi jeden origin, nie ma
żądań cross-origin, a backend nie zwraca żadnych nagłówków `Access-Control-*`.
Port backendu celowo nie jest publikowany na hoście — jedyne wejście prowadzi
przez nginx.

## Uruchomienie

Działa od razu po sklonowaniu, bez żadnych plików konfiguracyjnych:

```bash
docker compose up -d --build
```

Plik `.env` jest opcjonalny. Potrzebny tylko wtedy, gdy chcesz własne dane
do bazy zamiast domyślnych:

```bash
cp .env.example .env     # i podmień POSTGRES_PASSWORD
docker compose down -v   # nowe hasło działa dopiero na nowym wolumenie
docker compose up -d --build
```

- aplikacja: http://localhost:8080
- API przez proxy: http://localhost:8080/api/messages

```bash
docker compose down      # dane w bazie zostają (wolumen postgres-data)
docker compose down -v   # kasuje też wolumen
```

Testy jednostkowe sprawdzają walidację wiadomości: puste body, limit długości,
obcinanie białych znaków. Nie potrzebują bazy:

```bash
sbt test
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

**Domyślne hasło w `docker-compose.yml` jest jawne i to świadoma decyzja.**
`docker compose up` ma działać zaraz po sklonowaniu, dlatego Compose podstawia
hasło `postgres`, gdy `POSTGRES_PASSWORD` nie jest ustawione. Ta wartość żyje
tylko w pliku do lokalnego uruchomienia, a nie w obrazie. Baza nie publikuje
portu na hoście, więc widać ją wyłącznie z sieci Compose. W każdym innym
środowisku hasło przychodzi z sekretu, a brak konfiguracji zatrzymuje backend
przy starcie.

Poświadczeń do bazy się nie hashuje — sterownik JDBC musi wysłać je dosłownie.
Chroni się je zarządzaniem sekretem: `.env` poza repozytorium, dalej
`docker secret` montowany jako plik, docelowo Key Vault.

## Stack

Scala 2.13 + Cask, HikariCP, PostgreSQL 15, nginx, Docker Compose.
