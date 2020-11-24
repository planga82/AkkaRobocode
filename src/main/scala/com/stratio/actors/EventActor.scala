package com.stratio.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.stratio.actors.EventActor._
import com.stratio.actors.MoveActor.{ChangeDirection, Movements}

object EventActor{

  sealed trait Event
  case object OnHitByBullet extends Event
  case object StateOff extends Event
  case object StateOn extends Event

  sealed trait State
  case object On extends State
  case object Off extends State

  def apply(moveActor:ActorRef[Movements], state: State = On): Behavior[Event] =
    Behaviors.setup(context => new EventActor(context, moveActor, state))

}

class EventActor(context: ActorContext[Event], moveActor:ActorRef[Movements], state: State) extends AbstractBehavior[Event](context) {

  override def onMessage(msg: Event): Behavior[Event] = {
    (state, msg) match {
      case (On, OnHitByBullet) =>
        moveActor ! ChangeDirection
        this
      case (On, StateOff) =>
        EventActor(moveActor, Off)
      case (Off, StateOn) =>
        EventActor(moveActor, On)
      case other =>
        context.log.warn(s"Event not allowed in this state $other")
        this
    }
  }
}
