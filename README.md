![CI](https://github.com/kaamara/ScalaAkkaLearning/actions/workflows/ci.yml/badge.svg)

# ScalaAkkaLearning

Nauka Scali i Akki — zbiór małych, niezależnych projektów. Każdy ma własny
`build.sbt`, `Dockerfile` i README, więc otwiera się i uruchamia osobno.

## Projekty

| Projekt | Opis | Stack |
|---|---|---|
| [01-kalkulator](01-kalkulator) | Kalkulator na aktorach Akka: dodawanie, odejmowanie, mnożenie, dzielenie | Scala 2.13, Akka 2.6.21 |
| [02-licznik](02-licznik) | Licznik na aktorze Akka: increment, decrement, reset, odczyt | Scala 2.13, Akka 2.6.21 |
| [03-http-api](03-http-api) | REST na Akka HTTP: licznik plus `/health`, `/ready` i `/metrics` dla Prometheusa | Scala 2.13, Akka HTTP 10.2.10, Docker |
| [scala-docker-app](scala-docker-app) | Backend Cask + PostgreSQL + frontend nginx, całość na Docker Compose | Scala 2.13, Cask, PostgreSQL, Docker |

## Uruchomienie

W korzeniu repo nie ma `build.sbt` — każdy projekt uruchamia się ze swojego
folderu:

```bash
cd 03-http-api
sbt run          # albo: docker compose up -d --build
sbt test
```

W IntelliJ otwieraj `build.sbt` wybranego podprojektu, nie folder repo.
Szczegóły w README każdego z nich.

## CI

`.github/workflows/ci.yml`, wyzwalany przy pushu i PR do `main`:

- `build` — testy i obraz `01-kalkulator` (`kaamara/scala-akka-kalkulator`)
- `test-http-api` → `build-http-api` — testy i obraz `03-http-api`
  (`kaamara/scala-akka-http-api`), tagowany po `github.sha`

`02-licznik` nie ma jeszcze joba.

## Licencja Akka

Wszędzie używana jest Akka 2.6.x / Akka HTTP 10.2.x — ostatnie wersje na
Apache 2.0. Nowsze (2.7+) są na Business Source License i wymagają płatnej
licencji przy komercyjnym użyciu. Alternatywa dla nowego kodu: Apache Pekko.
