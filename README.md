![CI](https://github.com/kaamara/ScalaAkkaLearning/actions/workflows/ci.yml/badge.svg)

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

Przy pushu i pull requeście do `main`:

- `build` buduje obraz każdego projektu, a `Dockerfile` uruchamia przy tym testy.
- `smoke` podnosi `04-scala-docker-app` i sprawdza frontend, proxy i zapis do bazy.

Obrazy trafiają na Docker Hub tylko przy pushu do `main`, jeśli w repo są ustawione sekrety `DOCKERHUB_USERNAME` i `DOCKERHUB_TOKEN`.


DevOps, od najbardziej opłacalnych:
1. CI: paths w workflow (branch z 01 buduje tylko 01), skan obrazów Trivy i Dependabot.
2. Kubernetes dla 03: Deployment i Service z sondami na gotowe /health i /ready, uruchamiane lokalnie na kind, potem Helm i ArgoCD.
3. Monitoring: Grafana z gotowym dashboardem i alerty do istniejącego Prometheusa, a logi w Loki.
4. Baza w 04: migracje Flyway zamiast CREATE TABLE przy starcie, backup, hasło jako sekret zamiast w compose.
5. Terraform: wdrożenie na chmurę, np. Azure Container Apps z zarządzanym Postgresem.