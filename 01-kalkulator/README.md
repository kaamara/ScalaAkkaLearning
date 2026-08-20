# 01-kalkulator

Interaktywny kalkulator na aktorach Akka. Obsługuje dodawanie, odejmowanie,
mnożenie i dzielenie.

## Uruchomienie

```bash
sbt run
sbt test
```

W kontenerze (aplikacja czyta z konsoli, więc `-it` jest konieczne):

```bash
docker build -t kalkulator .
docker run --rm -it kalkulator
```

## Stack

Scala 2.13, Akka 2.6.21, obraz na Temurin 11.
