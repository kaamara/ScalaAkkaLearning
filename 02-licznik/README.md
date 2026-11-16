# 02-licznik

Licznik na aktorze Akka. Stan to zwykły `var` bez blokad, bo aktor przetwarza jedną wiadomość naraz. Pokazuje `!` (wyślij i nie czekaj) oraz `?` (zapytaj i odbierz odpowiedź).

```
sbt run
sbt test
```

W Dockerze: `docker build -t licznik .`, potem `docker run --rm licznik`. Wypisuje `Aktualny licznik: 2` i `Po resecie: 0`.
