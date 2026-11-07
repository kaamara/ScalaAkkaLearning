package pl.kaamara.httpapi

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class CounterActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "CounterActor" should {

    "zwrocic wartosc poczatkowa" in {
      val counter = spawn(CounterActor(0L))
      val probe   = createTestProbe[CounterActor.Value]()

      counter ! CounterActor.Get(probe.ref)
      probe.expectMessage(CounterActor.Value(0L))
    }

    "inkrementowac i utrzymywac stan miedzy wiadomosciami" in {
      val counter = spawn(CounterActor(0L))
      val probe   = createTestProbe[CounterActor.Value]()

      counter ! CounterActor.Increment(probe.ref)
      probe.expectMessage(CounterActor.Value(1L))

      counter ! CounterActor.Increment(probe.ref)
      probe.expectMessage(CounterActor.Value(2L))

      counter ! CounterActor.Get(probe.ref)
      probe.expectMessage(CounterActor.Value(2L))
    }

    "dekrementowac ponizej zera" in {
      val counter = spawn(CounterActor(0L))
      val probe   = createTestProbe[CounterActor.Value]()

      counter ! CounterActor.Decrement(probe.ref)
      probe.expectMessage(CounterActor.Value(-1L))
    }

    "resetowac stan do zera" in {
      val counter = spawn(CounterActor(41L))
      val probe   = createTestProbe[CounterActor.Value]()

      counter ! CounterActor.Increment(probe.ref)
      probe.expectMessage(CounterActor.Value(42L))

      counter ! CounterActor.Reset(probe.ref)
      probe.expectMessage(CounterActor.Value(0L))

      counter ! CounterActor.Get(probe.ref)
      probe.expectMessage(CounterActor.Value(0L))
    }

    "obsluzyc sekwencje wiadomosci bez gubienia inkrementow" in {
      val counter = spawn(CounterActor(0L))
      val probe   = createTestProbe[CounterActor.Value]()

      (1 to 100).foreach(_ => counter ! CounterActor.Increment(probe.ref))
      probe.receiveMessages(100)

      counter ! CounterActor.Get(probe.ref)
      probe.expectMessage(CounterActor.Value(100L))
    }
  }
}
