package pl.kaamara.httpapi

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

/** Aktor trzymajacy wartosc licznika.
  *
  * Wartosc nie jest zwyklym polem klasy. Po kazdej zmianie aktor zwraca
  * swoja nowa wersje z nowa liczba. Nikt z zewnatrz nie moze jej zmienic
  * wprost - trzeba wyslac wiadomosc. Na tym polegaja aktory.
  */
object CounterActor {

  sealed trait Command
  final case class Increment(replyTo: ActorRef[Value]) extends Command
  final case class Decrement(replyTo: ActorRef[Value]) extends Command
  final case class Get(replyTo: ActorRef[Value])       extends Command
  final case class Reset(replyTo: ActorRef[Value])     extends Command

  final case class Value(count: Long)

  def apply(initial: Long = 0L): Behavior[Command] = counting(initial)

  private def counting(current: Long): Behavior[Command] =
    Behaviors.receiveMessage {
      case Increment(replyTo) =>
        val next = current + 1
        replyTo ! Value(next)
        counting(next)

      case Decrement(replyTo) =>
        val next = current - 1
        replyTo ! Value(next)
        counting(next)

      case Get(replyTo) =>
        replyTo ! Value(current)
        Behaviors.same

      case Reset(replyTo) =>
        replyTo ! Value(0L)
        counting(0L)
    }
}
