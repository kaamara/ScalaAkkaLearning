![CI 01](https://github.com/kaamara/ScalaAkkaLearning/actions/workflows/ci-01-kalkulator.yml/badge.svg) ![CI 02](https://github.com/kaamara/ScalaAkkaLearning/actions/workflows/ci-02-licznik.yml/badge.svg) ![CI 03](https://github.com/kaamara/ScalaAkkaLearning/actions/workflows/ci-03-http-api.yml/badge.svg) ![CI 04](https://github.com/kaamara/ScalaAkkaLearning/actions/workflows/ci-04-scala-docker-app.yml/badge.svg)

# ScalaAkkaLearning

Małe, niezależne projekty do nauki Scali i Akki. Każdy uruchamia się ze swojego folderu.

| Projekt | Co robi | Uruchomienie |
|---|---|---|
| [01-kalkulator](01-kalkulator) | Kalkulator w konsoli na aktorze Akka | `sbt run` |
| [02-licznik](02-licznik) | Licznik na aktorze Akka | `sbt run` |
| [03-http-api](03-http-api) | Licznik przez REST z `/health`, `/ready` i `/metrics` | `docker compose up -d --build --wait` |
| [04-scala-docker-app](04-scala-docker-app) | Frontend, backend i baza w Docker Compose | `docker compose up -d --build --wait` |

Testy w każdym projekcie: `sbt test`.

`03` i `04` używają portu 8080, więc uruchamiaj je po kolei.

## CI

Każdy projekt ma osobny workflow, który odpala się tylko wtedy, gdy zmieni się jego folder (push i pull request do `main`):

- `ci-01-kalkulator.yml`, `ci-02-licznik.yml`, `ci-03-http-api.yml`, `ci-04-scala-docker-app.yml` wywołują `build-project.yml`, który buduje obraz, a `Dockerfile` uruchamia przy tym testy.
- `ci-04-scala-docker-app.yml` dodatkowo ma job `smoke`, który podnosi `04-scala-docker-app` i sprawdza frontend, proxy i zapis do bazy.

Zmiana w `build-project.yml` uruchamia wszystkie cztery projekty, a zmiana w jednym `ci-*.yml` tylko ten jeden.

Zbudowany obraz jest najpierw uruchamiany: `01` i `02` sprawdzane są po wyniku z konsoli, `03` odpytuje `/health`, `/counter/increment` i `/metrics`. Dopiero po udanym teście obraz może trafić na Docker Hub.