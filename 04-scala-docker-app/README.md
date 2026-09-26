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

```
docker compose up -d --build
```

Plik `.env` jest opcjonalny. Potrzebny tylko wtedy, gdy chcesz własne dane
do bazy zamiast domyślnych:

```
cp .env.example .env     # i podmień POSTGRES_PASSWORD
docker compose down -v   # nowe hasło działa dopiero na nowym wolumenie
docker compose up -d --build
```

- aplikacja: http://localhost:8080
- API przez proxy: http://localhost:8080/api/messages

```
docker compose down      # dane w bazie zostają (wolumen postgres-data)
docker compose down -v   # kasuje też wolumen
```

Testy jednostkowe sprawdzają walidację wiadomości: puste body, limit długości,
obcinanie białych znaków. Nie potrzebują bazy:

```
sbt test
```

## Endpointy

| Metoda | Ścieżka (przez proxy) | Opis |
|---|---|---|
| `GET` | `/api/` | Sprawdzenie, że backend żyje |
| `POST` | `/api/add` | Zapis wiadomości; body to zwykły tekst, max 500 znaków |
| `GET` | `/api/messages` | Pięć ostatnich wiadomości |

## Jak się pobawić

Po `docker compose up -d --build` otwórz `http://localhost:8080` i wpisz
wiadomość. Z terminala to samo:

```
curl -s localhost:8080/api/                          # "Backend gotowy!"
curl -s localhost:8080/api/messages                  # ostatnie 5
curl -s -X POST --data "czesc" localhost:8080/api/add
```

### Dwa różne 413

Wiadomość 501 znaków odrzuca backend:

```
curl -s -X POST --data "$(printf 'x%.0s' $(seq 501))" localhost:8080/api/add
# Wiadomosc dluzsza niz 500 znakow
```

Wiadomość 9000 znaków odrzuca nginx — request nawet nie dociera do Scali:

```
curl -s -X POST --data "$(printf 'x%.0s' $(seq 9000))" localhost:8080/api/add
# <html><head><title>413 Request Entity Too Large</title></head>...
```

Ten sam kod HTTP, inny nadawca. Limit nginxa to `client_max_body_size 8k`,
limit backendu to `MaxLength` w
[`MessageValidation.scala`](src/main/scala/MessageValidation.scala). Tania
kontrola odsiewa na brzegu, droga sprawdza szczegóły w aplikacji.

Puste body (albo same spacje) daje 400:

```
curl -s -X POST --data "   " localhost:8080/api/add   # Pusta wiadomosc
```

### Backendu nie da się ominąć

```
curl -s --max-time 3 localhost:8081/                     # odmowa polaczenia
docker compose exec frontend wget -qO- http://scala-app:8081/   # dziala
```

Port 8081 istnieje tylko w sieci Compose — w `docker-compose.yml` jest
`expose`, nie `ports`. To ta sama różnica, co `ClusterIP` kontra
`LoadBalancer` w Kubernetesie.

### Zajrzyj do bazy

```
docker compose exec db psql -U app -d mojabaza -c "SELECT * FROM messages;"
docker compose exec -it db psql -U app -d mojabaza    # interaktywnie, \q wychodzi
```

Dopisz wiersz ręcznie i odśwież stronę — pojawi się, bo frontend tylko czyta
z bazy:

```sql
INSERT INTO messages (content) VALUES ('wpisane recznie z psql');
```

### Pozostałe eksperymenty

- **Trwałość danych** — po `docker compose down` i `up -d` wiadomości nadal
  są, bo siedzą w wolumenie. Dopiero `down -v` je kasuje.
- **Zabij bazę** — `docker compose stop db`, potem POST na `/api/add`.
  Przy restarcie całości zobacz w logach retry: `docker compose logs scala-app | grep init`.
- **Usuń zmienną** — zakomentuj `DB_PASSWORD` w `docker-compose.yml`.
  Kontener nie wstanie i powie dlaczego. To decyzja opisana niżej
  w „Konfiguracja", zobaczona na własne oczy.
- **Szablon nginxa** — `docker compose exec frontend cat /etc/nginx/conf.d/default.conf`
  pokazuje config po podstawieniu `${BACKEND_PORT}`.
- **Zmiana portu** — `FRONTEND_PORT=9000` w `.env`. Przydaje się, gdy chcesz
  odpalić równolegle [03-http-api](../03-http-api), które też chce 8080.
- **Frontend na żywo** — katalog `frontend/` jest podmontowany jako wolumen,
  więc zmiana w `index.html` działa bez przebudowy obrazu. Wystarczy Ctrl+F5.
- **Kolejność startu** — uruchom bez `-d` i zobacz: db → healthy → scala-app
  → healthy → frontend. Robi to `depends_on: condition: service_healthy`.

### Pułapka z hasłem

Zmiana `POSTGRES_PASSWORD` w `.env` nie zadziała po zwykłym
`docker compose up -d`. Postgres ustawia hasło tylko przy pierwszej
inicjalizacji wolumenu — potrzebny jest `docker compose down -v`, czyli
utrata danych.

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
