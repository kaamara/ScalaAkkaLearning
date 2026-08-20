# 02-licznik

Licznik na aktorze Akka (`LicznikActor`). Obsługuje zwiększanie, zmniejszanie,
reset i odczyt bieżącego stanu.

## Uruchomienie

```bash
sbt run
sbt test
```

W kontenerze:

```bash
docker build -t licznik .
docker run --rm -it licznik
```

## Stack

Scala 2.13, Akka 2.6.21, obraz na Temurin 11.

Wersja z tym samym licznikiem wystawionym przez REST jest w
[03-http-api](../03-http-api).
