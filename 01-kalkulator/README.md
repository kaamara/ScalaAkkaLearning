# 01-kalkulator

Kalkulator w konsoli na aktorze Akka: `Parser` rozpoznaje działanie (np. `10 + 5`), a `KalkulatorActor` liczy i odsyła wynik.

```
sbt run
sbt test
```

W Dockerze: `docker build -t kalkulator .`, potem `docker run --rm -it kalkulator`.
