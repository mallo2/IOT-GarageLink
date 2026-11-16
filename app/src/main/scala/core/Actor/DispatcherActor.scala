package core.Actor

import core.FakeReading
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

class DispatcherActor(temperatureActor: ActorRef, particularActor: ActorRef) extends Actor {
  def receive: Receive = {
    case FakeReading("TempSensor", value, _)       => temperatureActor ! value
    case FakeReading("ParticularSensor", value, _) => particularActor ! value
    case FakeReading(id, _, _)                     => println(s"ignored $id")
  }
}
