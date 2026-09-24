# 02-licznik

Licznik na aktorze Akka (`LicznikActor`). Obsługuje zwiększanie, zmniejszanie,
reset i odczyt bieżącego stanu.

## Uruchomienie

```bash
sbt run
sbt test
```

W kontenerze (program wykonuje swój scenariusz i sam się kończy, więc `-it`
nie jest potrzebne):

```bash
docker build -t licznik .
docker run --rm licznik
```

## Stack

Scala 2.13, Akka 2.6.21, obraz na Temurin 17 (JRE).

Wersja z tym samym licznikiem wystawionym przez REST jest w
[03-http-api](../03-http-api).
