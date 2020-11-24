package com.stratio.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import MainActor._
import MoveActor._

object MoveActor {

  sealed trait Movements
  case object ChangeDirection extends Movements
  case class ChangeShape(shape: Shape) extends Movements

  sealed trait Rotation
  case object TurningLeft extends Rotation
  case object TurningRight extends Rotation

  sealed trait Shape
  case object Square extends Shape
  case object Circle extends Shape
  case object Line extends Shape
  case object Plus extends Shape

  val opposite: Map[Rotation, Rotation] = Map(TurningLeft -> TurningRight, TurningRight -> TurningLeft)

  val movements: Map[(Shape, Rotation), List[Action]] = Map(
      (Square, TurningLeft) -> List(Ahead(50), TurnLeft(90), Ahead(50), TurnLeft(90), Ahead(50), TurnLeft(90), Ahead(50), TurnLeft(90)),
      (Square, TurningRight) -> List(Ahead(50), TurnRight(90), Ahead(50), TurnRight(90), Ahead(50), TurnRight(90), Ahead(50), TurnRight(90)),
      (Circle, TurningLeft) -> List(1 to 36).map(_ => List(Ahead(20), TurnLeft(10))).flatten,
      (Circle, TurningRight) -> List(1 to 36).map(_ => List(Ahead(20), TurnRight(10))).flatten,
      (Plus, TurningLeft) -> List(1 to 4).map(_ => List(Ahead(100), Back(100), TurnLeft(90))).flatten,
      (Plus, TurningRight) -> List(1 to 4).map(_ => List(Ahead(100), Back(100), TurnRight(90))).flatten)

  def apply(mainActor:ActorRef[Command], state: Shape = Square, rotation: Rotation = TurningLeft): Behavior[Movements] =
    Behaviors.setup(context => new MoveActor(context, mainActor, state, rotation))
}

class MoveActor(context: ActorContext[Movements], mainActor:ActorRef[Command], shape: Shape, rotation: Rotation) extends AbstractBehavior[Movements](context) {

  override def onMessage(msg: Movements): Behavior[Movements] = {
    context.log.info(s"Msg received $msg")
    msg match {
      case ChangeDirection =>
        mainActor ! Actions(movements(shape, opposite(rotation)))
        MoveActor(mainActor, shape, opposite(rotation))
      case ChangeShape(s) =>
        mainActor ! Actions(movements(s, rotation))
        MoveActor(mainActor, s, rotation)
    }
  }

}
