![CI](https://github.com/kaamara/ScalaAkkaLearning/actions/workflows/ci.yml/badge.svg)

# ScalaAkkaLearning

Małe, niezależne projekty do nauki Scali, Akki i DevOpsa. Każdy ma własny `Dockerfile` i uruchamia się ze swojego folderu.

| Projekt | Co robi | Uruchomienie |
|---|---|---|
| [01-kalkulator](01-kalkulator) | Kalkulator w konsoli na aktorze Akka | `sbt run` |
| [02-licznik](02-licznik) | Licznik na aktorze Akka | `sbt run` |
| [03-http-api](03-http-api) | Licznik przez REST z `/health`, `/ready` i `/metrics` | `docker compose up -d --build --wait` |
| [04-scala-docker-app](04-scala-docker-app) | Frontend, backend i baza w Docker Compose | `docker compose up -d --build --wait` |

Testy: `sbt test`. `03` i `04` używają portu 8080, więc uruchamiaj je po kolei.

## CI

GitHub Actions, jeden workflow `ci.yml` na push i pull request do `main`:

- buduje tylko projekty, których folder się zmienił,
- testy jednostkowe idą w trakcie `docker build`, potem `smoke.sh` sprawdza działający kontener,
- po udanym teście obraz trafia na Docker Hub z tagami `latest` i SHA commita (tylko push do `main`).
