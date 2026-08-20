![CI](https://github.com/kaamara/ScalaAkkaProjects/actions/workflows/ci.yml/badge.svg)

# ScalaAkkaProjects

Nauka Scala i Akka — zbiór małych, niezależnych projektów, każdy w swoim
podfolderze z własnym `build.sbt` i `Dockerfile`.

## Projekty

| Projekt | Opis | Stack |
|---|---|---|
| [01-kalkulator](01-kalkulator) | Interaktywny kalkulator działający na aktorach Akka (dodawanie, odejmowanie, mnożenie, dzielenie) | Scala 2.13, Akka 2.6.21 |
| [02-licznik](02-licznik) | Licznik oparty na aktorze Akka (increment / decrement / reset / odczyt stanu) | Scala 2.13, Akka 2.6.21 |
| [03-http-api](03-http-api) | REST na Akka HTTP + typed actors: licznik oraz `/health`, `/ready`, `/metrics` w formacie Prometheusa | Scala 2.13, Akka 2.6.20, Akka HTTP 10.2.10, Docker |
| [scala-docker-app](scala-docker-app) | Aplikacja webowa: backend Cask + PostgreSQL + statyczny frontend, całość odpalana przez `docker-compose` | Scala 2.13, Cask, PostgreSQL, Docker |

Szczegóły uruchomienia każdego projektu znajdują się w README w jego folderze.

## CI/CD

Workflow w `.github/workflows/ci.yml`:

- `build` — kompiluje, testuje i buduje obraz `01-kalkulator`
  (`kaamara/scala-akka-kalkulator`)
- `test-http-api` / `build-http-api` — testy i obraz `03-http-api`
  (`kaamara/scala-akka-http-api`), tagowany po `github.sha`

`02-licznik` nie ma jeszcze joba w workflow.