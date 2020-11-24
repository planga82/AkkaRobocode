package com.stratio.actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior, PreRestart, Signal}
import com.stratio.actors.EventActor.{Event, StateOff, StateOn}
import com.stratio.actors.KeyEventActor.{KeyEvent, KeyEventMsg, Working}
import com.stratio.actors.MoveActor._

import scala.concurrent.duration._

object KeyEventActor {

  sealed trait KeyEventMsg
  case class KeyEvent(key: Char) extends KeyEventMsg
  case object Working extends KeyEventMsg

  def apply(eventActor: ActorRef[Event], moveActor: ActorRef[Movements]): Behavior[KeyEventMsg] =
    Behaviors.setup(context =>
      Behaviors.withTimers(timers => new KeyEventActor(context, timers, eventActor, moveActor)))
}

class KeyEventActor(context: ActorContext[KeyEventMsg],timers: TimerScheduler[KeyEventMsg], eventActor:ActorRef[Event], moveActor: ActorRef[Movements]) extends AbstractBehavior[KeyEventMsg](context) {

  context.log.info("Key event Actor Created")
  timers.startSingleTimer(Working, 5 seconds)

  override def onMessage(msg: KeyEventMsg): Behavior[KeyEventMsg] = {
    msg match {
      case KeyEvent('q') => eventActor ! StateOff
      case KeyEvent('w') => eventActor ! StateOn
      case KeyEvent('s') => moveActor ! ChangeShape(Square)
      case KeyEvent('c') => moveActor ! ChangeShape(Circle)
      case KeyEvent('p') => moveActor ! ChangeShape(Plus)
      case KeyEvent('k') => throw new Exception("Key Event K -> Throw an exception")
      case Working =>
        context.log.info(s"I'm working")
        timers.startSingleTimer(Working, 1 seconds)
      case k => context.log.info(s"Key not recognized: $k")
    }
    this
  }

  override def onSignal: PartialFunction[Signal, Behavior[KeyEventMsg]] = {
    case PreRestart =>
      context.log.warn("Restarting actor")
      Behaviors.same
  }

}
