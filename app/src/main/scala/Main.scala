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

