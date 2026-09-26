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

```
sbt run     # serwer żyje do Ctrl+C
sbt test    # 16 testów
```

W kontenerze:

```
docker compose up -d --build
docker compose ps                      # czekaj na "(healthy)" — JVM wstaje kilka sekund
curl -s localhost:8080/health
docker compose down
```

Endpointy można też przeklikać z pliku [`requests.http`](requests.http)
(IntelliJ, plugin restClient) zamiast wpisywać curle.

## Prometheus

Startuje tylko z profilem `observability`, nie przy zwykłym `docker compose up`:

```
docker compose --profile observability up -d
```

Otwórz `http://localhost:9090`. Zapytania wpisujesz w pasek z lupką na górze
strony — Enter, wynik pokazuje się w zakładce **Table**.

| Zapytanie | Co pokazuje |
|-----------|-------------|
| `up` | czy Prometheus dociera do aplikacji: `1` = tak, `0` = nie |
| `sum by (status) (http_requests_total)` | requesty wg kodu odpowiedzi |
| `sum by (path) (http_requests_total)` | requesty wg ścieżki |
| `sum by (status) (rate(http_requests_total[1m]))` | requesty na sekundę |

Stan scrape'ów widać też bez pisania zapytań: menu **Status → Targets**.

### Widzisz tylko `status="200"`?

Seria czasowa powstaje dopiero wtedy, gdy dany przypadek faktycznie wystąpi.
Wygeneruj błędy i poczekaj 15 sekund na kolejny scrape:

```
curl -s localhost:8080/nie-ma-takiego              # 404
curl -s -X GET localhost:8080/counter/increment    # 405, bo to endpoint POST
```

### Warto zauważyć

- **Liczby spóźniają się o jeden scrape** — Prometheus odczytuje licznik co
  15 sekund, nie widzi pojedynczych zdarzeń.
- **`_total` tylko rośnie**, dlatego prawie zawsze opakowuje się to w
  `rate()`. Różnicę najlepiej widać na zakładce **Graph**.
- **Nie wszystkie requesty są Twoje** — `/health` nabija healthcheck Dockera,
  a `/metrics` sam Prometheus.
- **`docker compose stop api`** → `up` spada do `0`, ale `http_requests_total`
  zostaje na ostatniej wartości. Po `start` licznik rusza od zera — dlatego
  `rate()` radzi sobie z restartami, a odejmowanie surowych wartości nie.

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

**Obraz multi-stage, non-root, 421 MB.** Etap build stoi na przypiętym tagu
z konkretną wersją JDK, sbt i Scali, więc kompilacja jest powtarzalna. Runtime
`eclipse-temurin:17-jre-jammy` celowo nie jest przypięty do patcha: każdy
rebuild dostaje najnowsze poprawki bezpieczeństwa JRE i Ubuntu. Ceną jest to,
że dwa buildy z tego samego commita mogą mieć inną warstwę bazową. JVM dostaje
`MaxRAMPercentage` zamiast `-Xmx`, żeby respektowała limit pamięci kontenera.


