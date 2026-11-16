# 04-scala-docker-app

Trzy kontenery w Docker Compose: nginx (strona i proxy `/api/`), backend w Scali (Cask) i PostgreSQL. Na hoście port ma tylko nginx.

Utwórz plik `.env` z linią `POSTGRES_PASSWORD=twoje-haslo`, potem:

```
docker compose up -d --build --wait
```

Aplikacja: http://localhost:8080. `docker compose down -v` kasuje też dane z bazy. Testy: `sbt test`.

- `GET /api/`: czy backend żyje
- `POST /api/add`: zapis wiadomości (maks. 500 znaków)
- `GET /api/messages`: pięć ostatnich wiadomości
