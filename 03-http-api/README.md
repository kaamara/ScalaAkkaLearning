# 03-http-api

Akka HTTP + typed actors. Stanowy licznik wystawiony przez REST, z endpointami
operacyjnymi gotowymi pod Kubernetesa i Prometheusa.

## Endpointy

| Metoda | Ścieżka              | Opis                                       |
|--------|----------------------|--------------------------------------------|
| GET    | `/health`            | liveness — proces żyje                     |
| GET    | `/ready`             | readiness — przyjmuje ruch (503 przy shutdownie) |
| GET    | `/counter`           | aktualna wartość                           |
| POST   | `/counter/increment` | +1                                         |
| POST   | `/counter/decrement` | -1                                         |
| POST   | `/counter/reset`     | zerowanie                                  |
| GET    | `/metrics`           | metryki w formacie Prometheusa             |

## Uruchomienie

```bash
sbt run     # serwer żyje do Ctrl+C
sbt test    # 16 testów
```

W kontenerze:

```bash
docker compose up -d --build
docker compose ps                      # czekaj na "(healthy)" — JVM wstaje kilka sekund
curl -s localhost:8080/health
docker compose down
```

Endpointy można też przeklikać z pliku [`requests.http`](requests.http)
(IntelliJ, plugin restClient) zamiast wpisywać curle.

## Prometheus

```bash
docker compose --profile observability up -d
```

Prometheus na `http://localhost:9090`, scrapuje `/metrics` co 15 sekund.
Przykładowe zapytanie: `sum by (status) (http_requests_total)`. Bez tego
profilu startuje sama aplikacja.

## Decyzje projektowe

**`/health` i `/ready` to dwa różne endpointy.** Mapują się na `livenessProbe`
i `readinessProbe`. Porażka liveness restartuje poda, porażka readiness tylko
wyjmuje go z Service. Podpięcie obu prób pod jeden endpoint sprawia, że
przeciążona aplikacja wpada w pętlę restartów zamiast po prostu przestać
dostawać ruch.

**Graceful shutdown przed unbindem.** Po SIGTERM aplikacja najpierw zwraca 503
na `/ready` i czeka 3 sekundy, zanim zamknie połączenia. Bez tego okna load
balancer przez chwilę kieruje ruch do zamykającego się procesu.

**Stan w parametrze `Behavior`, nie w polu klasy.** Aktor nie ma `var`; kolejny
stan to nowe zachowanie zwrócone z `Behaviors.receiveMessage`.

**Metryki bez biblioteki klienckiej.** Format tekstowy Prometheusa jest
generowany ręcznie, żeby było widać jego strukturę. W projekcie produkcyjnym
używa się `micrometer` albo `prometheus-client`.

**Normalizacja ścieżek w metrykach.** `/counter/123` zapisuje się jako
`/counter/{id}`, a każde 404 jako `/{unmatched}`. Bez tego każdy unikalny URL
tworzy osobną serię czasową — klasyczny sposób na przewrócenie Prometheusa.

**Obraz multi-stage, non-root, 421 MB.** Tag bazowy pinowany, a JVM dostaje
`MaxRAMPercentage` zamiast `-Xmx`, żeby respektowała limit pamięci kontenera.


