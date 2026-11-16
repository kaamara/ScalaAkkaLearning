# 03-http-api

REST API licznika (Akka HTTP, typowany aktor) z sondami, metrykami dla Prometheusa i czystym zamykaniem po SIGTERM.

- `GET /counter`, `POST /counter/increment|decrement|reset`
- `GET /health`, `GET /ready` (503 podczas zamykania), `GET /metrics`

```
docker compose up -d --build --wait
curl -X POST localhost:8080/counter/increment
docker compose down
```

Prometheus: http://localhost:9090. Testy: `sbt test`.
