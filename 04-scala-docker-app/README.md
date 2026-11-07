# 04-scala-docker-app

Trzy kontenery: frontend na nginx, backend w Scali (Cask) i baza PostgreSQL.

nginx serwuje stronę i przekazuje `/api/` do backendu. Tylko nginx ma port na hoście.

## Uruchomienie

Utwórz plik `.env` z hasłem do bazy (same litery i cyfry). Jest w `.gitignore`, więc nie trafi do repo:

```
POSTGRES_PASSWORD=twoje-haslo
```

Potem uruchom aplikację:

```
docker compose up -d --build --wait
```

Baza ustawia hasło tylko przy pierwszym starcie. Żeby je zmienić, skasuj dane przez `docker compose down -v`.

Aplikacja działa pod http://localhost:8080.

`docker compose down` zatrzymuje aplikację, a `docker compose down -v` kasuje też dane z bazy.

Testy walidacji wiadomości (bez bazy): `sbt test`.

## Endpointy

| Metoda | Ścieżka | Opis |
|---|---|---|
| GET | `/api/` | Czy backend żyje |
| POST | `/api/add` | Zapis wiadomości: tekst w body, maks. 500 znaków |
| GET | `/api/messages` | Pięć ostatnich wiadomości |
