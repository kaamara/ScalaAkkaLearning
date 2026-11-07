# 02-licznik

`LicznikActor` trzyma stan w zwykłym `var count`. Nie potrzebuje blokad, bo przetwarza jedną wiadomość naraz.

- `licznik ! Increment` wysyła wiadomość i nie czeka na odpowiedź.
- `licznik ? GetCount` zwraca `Future` z odpowiedzią aktora.

```
sbt run
sbt test
```

W Dockerze:

```
docker build -t licznik .
docker run --rm licznik
```

Wynik:

```
Aktualny licznik: 2
Po resecie: 0
```
