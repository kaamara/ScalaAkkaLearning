# 01-kalkulator

Kalkulator w konsoli. `Main` czyta działanie z klawiatury, `Parser` je rozpoznaje, a `KalkulatorActor` liczy i odsyła wynik.

Działanie wpisuje się jak `10 + 5` (operatory `+ - * /`). `exit` kończy program.

```
sbt run
sbt test
```

W Dockerze (`-it`, bo program czyta z klawiatury):

```
docker build -t kalkulator .
docker run --rm -it kalkulator
```
