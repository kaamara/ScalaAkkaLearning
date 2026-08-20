# 03-http-api

Akka HTTP + typed actors. Stanowy licznik wystawiony przez REST, z endpointami
operacyjnymi gotowymi pod Kubernetes i Prometheusa.

Projekt zamyka serie Akka: zastepuje planowane Ping-Pong, Echo Server i czat
miedzy aktorami — komunikacja, obsluga HTTP i wspoldzielony stan sa tutaj
w jednym miejscu, w formie, ktora da sie wdrozyc.

## Endpointy

| Metoda | Sciezka             | Opis                                      |
|--------|---------------------|-------------------------------------------|
| GET    | `/health`           | liveness — proces zyje                    |
| GET    | `/ready`            | readiness — przyjmuje ruch (503 przy shutdown) |
| GET    | `/counter`          | aktualna wartosc                          |
| POST   | `/counter/increment`| +1                                        |
| POST   | `/counter/decrement`| -1                                        |
| POST   | `/counter/reset`    | zerowanie                                 |
| GET    | `/metrics`          | metryki w formacie Prometheusa            |

## Uruchomienie

```bash
# lokalnie
sbt run

# testy
sbt test

# kontener
docker compose up --build
curl -s localhost:8080/health
curl -s -X POST localhost:8080/counter/increment
curl -s localhost:8080/metrics
```

## Decyzje projektowe

**`/health` i `/ready` to dwa rozne endpointy.** W Kubernetesie mapuja sie na
`livenessProbe` i `readinessProbe`. Porazka liveness restartuje poda, porazka
readiness tylko wyjmuje go z Service. Podpiecie obu prob pod jeden endpoint
powoduje, ze przeciazona aplikacja wpada w petle restartow zamiast po prostu
przestac dostawac ruch.

**Graceful shutdown przed unbindem.** Po SIGTERM aplikacja najpierw zwraca 503
na `/ready` i odczekuje 3 sekundy, zanim zamknie polaczenia. Bez tego okna load
balancer przez chwile kieruje ruch do zamykajacego sie procesu i czesc zadan
dostaje blad.

**Metryki bez biblioteki klienckiej.** Format tekstowy Prometheusa jest
generowany recznie, zeby bylo widac jego strukture. W projekcie produkcyjnym
uzywa sie `micrometer` albo `prometheus-client`.

**Normalizacja sciezek w metrykach.** `/counter/123` jest zapisywane jako
`/counter/{id}`. Bez tego kazdy unikalny URL tworzy osobna serie czasowa —
klasyczny sposob na przewrocenie Prometheusa.

**Stan w parametrze `Behavior`, nie w polu klasy.** Aktor nie ma `var`;
kolejny stan to nowe zachowanie zwrocone z `Behaviors.receiveMessage`.

**Obraz: multi-stage, non-root, pinowany tag bazowy.** `MaxRAMPercentage`
zamiast `-Xmx`, zeby JVM respektowala limit pamieci kontenera.

## Rozmiar obrazu

| Wariant                              | Rozmiar |
|--------------------------------------|---------|
| single-stage (sbt w obrazie koncowym) | _niezmierzone_ |
| multi-stage, JRE                      | 421 MB |

Zmierz: `docker images kaamara/scala-akka-http-api --format "{{.Size}}"`

## Licencja Akka

Uzyte wersje: Akka 2.6.20, Akka HTTP 10.2.10 — ostatnie na Apache 2.0.
Nowsze (2.7+) sa na Business Source License. Alternatywa dla nowego kodu:
Apache Pekko (fork Akka 2.6 rozwijany przez ASF).
