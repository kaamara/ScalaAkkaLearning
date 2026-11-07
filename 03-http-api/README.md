# 03-http-api

Licznik w typowanym aktorze Akka, wystawiony przez REST (Akka HTTP), z metrykami dla Prometheusa.

| Metoda | Ścieżka | Opis |
|---|---|---|
| GET | `/health` | Czy proces żyje |
| GET | `/ready` | Czy przyjmuje ruch (503 podczas zamykania) |
| GET | `/counter` | Aktualna wartość |
| POST | `/counter/increment` | +1 |
| POST | `/counter/decrement` | -1 |
| POST | `/counter/reset` | Zerowanie |
| GET | `/metrics` | Metryki dla Prometheusa |

## Uruchomienie

```
docker compose up -d --build --wait
curl -X POST localhost:8080/counter/increment
curl localhost:8080/metrics
docker compose down
```

Prometheus działa pod http://localhost:9090. Przykładowe zapytanie: `sum by (status) (http_requests_total)`.

Bez Dockera: `sbt run` (serwer działa do Ctrl+C). Testy: `sbt test`.
