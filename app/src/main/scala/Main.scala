import core.actor.datagetter.{RFIDListenerActor, UDPListenerActor}
import core.actor.{DispatcherActor, ParticularActor, TemperatureActor, RFIDActor}
import core.api.HttpServer
import core.api.controllers.{HealthController, TemperatureController}
import core.config.AppConfig
import org.apache.pekko.actor.typed.scaladsl.adapter.*
import org.apache.pekko.actor.{ActorRef, ActorSystem}
import org.apache.pekko.actor.typed

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

final case class Actors(
  temperature: ActorRef,
  particular: ActorRef,
  rfid: ActorRef,
  dispatcher: ActorRef,
  udpListener: ActorRef,
  rfidListener: ActorRef
)

private def createActors()(using system: ActorSystem): Actors =
  val temperature = system.actorOf(TemperatureActor.props(), "temp")
  val particular  = system.actorOf(ParticularActor.props(), "particular")
  val rfid        = system.actorOf(RFIDActor.props(), "rfid")
  val dispatcher  = system.actorOf(DispatcherActor.props(temperature, particular, rfid), "filter")
  val udpListener = system.actorOf(UDPListenerActor.props(dispatcher, AppConfig.udpHost, AppConfig.udpPort), "udp")
  val rfidListener = system.actorOf(RFIDListenerActor.props(dispatcher), "rfid-listener")

  Actors(temperature, particular, rfid, dispatcher, udpListener, rfidListener)

private def startHttpServer(actors: Actors)(using typed.ActorSystem[Nothing], ExecutionContext): Unit =
  HttpServer
    .start(
      controllers = List(
        HealthController(),
        TemperatureController(actors.temperature)
      ),
      host = AppConfig.httpHost,
      port = AppConfig.httpPort,
      allowedOrigins = AppConfig.corsAllowedOrigins
    )
    .foreach { binding =>
      val port = binding.localAddress.getPort
      println(s"Server started at http://localhost:$port")
      println(s"Swagger UI: http://localhost:$port/docs")
    }

@main def main(): Unit =
  given system: ActorSystem                     = ActorSystem("iot")
  given typedSystem: typed.ActorSystem[Nothing] = system.toTyped
  given ExecutionContext                        = system.dispatcher

  val actors = createActors()
  startHttpServer(actors)

  sys.addShutdownHook(system.terminate())
  Await.result(system.whenTerminated, Duration.Inf)