![CI](https://github.com/kaamara/ScalaAkkaProjects/actions/workflows/ci.yml/badge.svg)

# ScalaAkkaProjects

Nauka Scala i Akka — zbiór małych, niezależnych projektów, każdy w swoim
podfolderze z własnym `build.sbt` i `Dockerfile`.

## Projekty

| Projekt | Opis | Stack |
|---|---|---|
| [01-kalkulator](01-kalkulator) | Interaktywny kalkulator działający na aktorach Akka (dodawanie, odejmowanie, mnożenie, dzielenie) | Scala 2.13, Akka 2.6.21 |
| [02-licznik](02-licznik) | Licznik oparty na aktorze Akka (increment / decrement / reset / odczyt stanu) | Scala 2.13, Akka 2.6.21 |
| [scala-docker-app](scala-docker-app) | Aplikacja webowa: backend Cask + PostgreSQL + statyczny frontend, całość odpalana przez `docker-compose` | Scala 2.13, Cask, PostgreSQL, Docker |

Szczegóły uruchomienia każdego projektu znajdują się w README w jego folderze.

## CI/CD

Workflow w `.github/workflows/ci.yml` kompiluje i testuje `01-kalkulator`
oraz `02-licznik`, buduje ich obrazy Docker i publikuje je na Docker Hub
(`kaamara/scala-akka-kalkulator`, `kaamara/scala-akka-licznik`).
