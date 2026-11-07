package pl.kaamara.httpapi

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object CounterActor {

  sealed trait Command
  final case class Increment(replyTo: ActorRef[Value]) extends Command
  final case class Decrement(replyTo: ActorRef[Value]) extends Command
  final case class Get(replyTo: ActorRef[Value])       extends Command
  final case class Reset(replyTo: ActorRef[Value])     extends Command

  final case class Value(count: Long)

  def apply(count: Long = 0L): Behavior[Command] =
    Behaviors.receiveMessage {
      case Increment(replyTo) => reply(replyTo, count + 1)
      case Decrement(replyTo) => reply(replyTo, count - 1)
      case Get(replyTo)       => reply(replyTo, count)
      case Reset(replyTo)     => reply(replyTo, 0L)
    }

  private def reply(replyTo: ActorRef[Value], count: Long): Behavior[Command] = {
    replyTo ! Value(count)
    apply(count)
  }
}
