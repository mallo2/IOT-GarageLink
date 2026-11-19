import core.actor.RFIDListenerActor
import org.apache.pekko.actor.{Actor, ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class PrintActor extends Actor {
  def receive: Receive = {
    case msg => println(s"Reçu : $msg")
  }
}

@main def testRfid(): Unit =
  val system  = ActorSystem("test-rfid")
  val printer = system.actorOf(Props(new PrintActor), "printer")
  system.actorOf(RFIDListenerActor.props(printer), "rfid")

  sys.addShutdownHook(system.terminate())
  println("En attente du lecteur... passe un badge (Ctrl+C pour arreter)")

  Await.result(system.whenTerminated, Duration.Inf) // garde le programme en vie