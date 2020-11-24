package com.stratio.robocode

import java.awt.event

import akka.actor.typed.{ActorSystem, SupervisorStrategy}
import akka.actor.typed.scaladsl.{Behaviors, Routers}
import akka.util.Timeout
import com.stratio.actors.EventActor.OnHitByBullet
import com.stratio.actors.KeyEventActor.KeyEvent
import com.stratio.actors.{EventActor, KeyEventActor, MainActor, MoveActor}
import robocode._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Random



class robotest extends Robot {

  import akka.actor.typed.scaladsl.AskPattern._
  implicit lazy val timeout: Timeout = 3 seconds
  implicit lazy val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "AkkaSystem" + Random.nextInt(20))
  implicit val ec = system.executionContext

  val mainActor = system.systemActorOf(
    Behaviors.supervise(MainActor()).onFailure[Exception](SupervisorStrategy.restart), "mainActor")
  val moveActor = system.systemActorOf(
    Behaviors.supervise(MoveActor(mainActor)).onFailure[Exception](SupervisorStrategy.resume), "moveActor")
  val eventActor = system.systemActorOf(
    Behaviors.supervise(EventActor(moveActor)).onFailure[Exception](SupervisorStrategy.stop), "eventActor")
  val keyEventActor = system.systemActorOf(
    Routers.pool(poolSize = 4)(
      Behaviors
        .supervise(KeyEventActor(eventActor, moveActor))
        .onFailure[Exception](
          SupervisorStrategy.restartWithBackoff(minBackoff = 3 seconds, maxBackoff = 10 seconds, randomFactor = 0.2)))
      .withRoundRobinRouting(),
    "eventKeyActor")

  override def run(): Unit = {
    while (true) {

      val result: Future[MainActor.Action] = mainActor ? (ref => MainActor.GiveMeNextAction(ref))
      println("Message sent, waiting for response")
      Await.result(result, 2 seconds) match {
        case MainActor.Ahead(distance) => ahead(distance)
        case MainActor.Back(distance) => back(distance)
        case MainActor.TurnRight(degrees) => turnRight(degrees)
        case MainActor.TurnLeft(degrees) => turnLeft(degrees)
      }
    }
  }

  override def onScannedRobot(e: ScannedRobotEvent): Unit = {
    fire(2)
    super.onScannedRobot(e)
  }

  override def onHitByBullet(event: HitByBulletEvent): Unit = {
    eventActor ! OnHitByBullet
    super.onHitByBullet(event)
  }

  override def onKeyPressed(e: event.KeyEvent): Unit = {
    keyEventActor ! KeyEvent(e.getKeyChar)
    super.onKeyPressed(e)
  }

  override def onRoundEnded(event: RoundEndedEvent): Unit = {
    println("exit system")
    system.terminate()
    Await.ready(system.whenTerminated, 1 minute)
    super.onRoundEnded(event)
  }
}
