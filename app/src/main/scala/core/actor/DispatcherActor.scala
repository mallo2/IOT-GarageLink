package core.actor

import core.datatype.ReceivedData
import core.traits.Logging
import org.apache.pekko.actor.{Actor, ActorRef, Props}


object DispatcherActor {
   case class FakeReading(id: String, value: Double, time: Int)

  /**
   * Create Props for a DispatcherActor .
   *
   * @param temperatureActor The actor to which the temperature data will be dispatched.
   * @param particularActor The actor to which the particular data will be dispatched
   *
   * @return a Props for creating a DispatcherActor
   */
   def props(temperatureActor: ActorRef, particularActor: ActorRef): Props = Props(new DispatcherActor(temperatureActor,particularActor))
}

class DispatcherActor(temperatureActor: ActorRef, particularActor: ActorRef) extends Actor,Logging {
  def receive: Receive = {
    case v             => logger.debug(s"ignored $v")
  }
}
