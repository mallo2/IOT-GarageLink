package app

import core.actor.{DispatcherActor, ParticularActor, TemperatureActor, UDPListenerActor}
import org.apache.pekko.actor.ActorSystem
import core.config.AppConfig

@main def main(): Unit = {

  // Initiating Actors
  val system = ActorSystem("iot")
  val temperatureActor = system.actorOf(TemperatureActor.props(), "temp")
  val particularActor = system.actorOf(ParticularActor.props(),"particular")
  val dispatcherActor = system.actorOf(DispatcherActor.props(temperatureActor,particularActor), "filter")
  val udpListenerActor = system.actorOf(UDPListenerActor.props(dispatcherActor,AppConfig.udpHost,AppConfig.udpPort ), "udp")


  //Sleeping to weight sended data
  Thread.sleep(100000)
  system.terminate()
}


@main def run(): Unit =
  given ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "iot-http-server")

  given ExecutionContext = summon[ActorSystem[Nothing]].executionContext

  val host = "0.0.0.0"
  val port = 8080

  HttpServer
    .start(controllers = List(HealthController()), host = host, port = port)
    .foreach { binding =>
      println(s"Server started at http://localhost:${binding.localAddress.getPort}")
      println(s"Swagger UI: http://localhost:${binding.localAddress.getPort}/docs")
    }