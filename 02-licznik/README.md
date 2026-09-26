# 02-licznik

Licznik na aktorze Akka (`LicznikActor`). Obsługuje zwiększanie, zmniejszanie,
reset i odczyt bieżącego stanu.

## Uruchomienie

```
sbt run
sbt test
```

W kontenerze (program wykonuje swój scenariusz i sam się kończy, więc `-it`
nie jest potrzebne):

```
docker build -t licznik .
docker run --rm licznik
```

## Co robi po `sbt run`

```
Aktualny licznik: 2
Po resecie: 0
```

Trzy razy `Increment`, raz `Decrement` — stąd 2. Potem `Reset`.

## Jak to działa

Aktor trzyma stan w zwykłym `var count`, bez żadnego locka. Może tak być,
bo przetwarza **jedną wiadomość naraz**, po kolei ze swojej skrzynki.

Dwa sposoby wysyłania:

- `licznik ! Increment` — wrzuca do skrzynki i wraca od razu, bez odpowiedzi
- `licznik ? GetCount` — zwraca `Future` z odpowiedzią; aktor odsyła ją przez
  `sender() ! count`. `Await.result` czeka na wynik, żeby `main` zdążył go
  wypisać.

## Sprawdzenie w konsoli

```
sbt console
```

```scala
import akka.actor._, akka.pattern.ask, akka.util.Timeout
import scala.concurrent.duration._, scala.concurrent.Await

implicit val system: ActorSystem = ActorSystem("test")
implicit val t: Timeout = Timeout(3.seconds)
val licznik = system.actorOf(Props[LicznikActor]())

licznik ! Increment
licznik ! Increment
licznik ! Decrement
Await.result(licznik ? GetCount, 3.seconds)   // 1

system.terminate()
```

Wyjście z konsoli: `:quit`. Nie nazywaj systemu `sys` — ta nazwa koliduje
z pakietem `scala.sys` i konsola zgłosi błąd.

Tryb watch: zapisujesz plik, sbt sam kompiluje i uruchamia ponownie.

```
sbt "~run"
sbt "~test"
```

## Eksperymenty

**Nieznana wiadomość nie wywala aktora** — leci do *dead letters*, stan
zostaje bez zmian:

```scala
licznik ! "cokolwiek"
```

**Pytanie bez odpowiedzi kończy się timeoutem** po 3 sekundach, bo
`Increment` niczego nie odsyła:

```scala
Await.result(licznik ? Increment, 3.seconds)
```

**Licznik jest odporny na wiele wątków** — dopisz na końcu `Main`:

```scala
val watki = (1 to 8).map(_ => new Thread(() => (1 to 1250).foreach(_ => licznik ! Increment)))
watki.foreach(_.start()); watki.foreach(_.join())
Thread.sleep(500)
println(Await.result(licznik ? GetCount, 3.seconds))   // dokładnie 10000
```

Zwykłe pole klasy pisane z ośmiu wątków dawałoby losowe liczby.
(`.par` tu nie zadziała — w Scali 2.13 wymaga osobnej zależności.)

**Dodaj wiadomość z parametrem:**

```scala
case class Add(n: Int)
// w receive:
case Add(n) => count += n
```

**Zepsuj test celowo** i zrób `docker build -t licznik .` — obraz nie
powstanie, bo `Dockerfile` puszcza testy przed `assembly`.

## Stack

Scala 2.13, Akka 2.6.21, obraz na Temurin 17 (JRE).

Wersja z tym samym licznikiem wystawionym przez REST jest w
[03-http-api](../03-http-api).
