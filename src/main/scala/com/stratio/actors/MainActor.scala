package com.stratio.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.stratio.actors.MoveActor.{Square, TurningRight}


object MainActor {
  sealed trait Command
  case class GiveMeNextAction(replyTo: ActorRef[Action]) extends Command
  case class Actions(act: List[Action]) extends Command

  sealed trait Action
  case class Ahead(distance: Int) extends Action
  case class Back(distance: Int) extends Action
  case class TurnRight(degrees: Int) extends Action
  case class TurnLeft(degrees: Int) extends Action

  def apply(actions: List[Action] = MoveActor.movements(Square, TurningRight), index: Int = 0): Behavior[Command] =
    Behaviors.setup(context => new MainActor(context,actions, index))

}

import MainActor._

class MainActor (context: ActorContext[Command], actions: List[Action], index: Int) extends AbstractBehavior[Command](context) {

  override def onMessage(msg: Command): Behavior[Command] = {
    context.log.info(s"Message received: $msg")
    msg match {
      case GiveMeNextAction(replyTo) =>
        val act = actions(index)
        replyTo ! act
        MainActor(actions, (index + 1) % actions.length)
      case Actions(list) => MainActor(list)
    }
  }
}
