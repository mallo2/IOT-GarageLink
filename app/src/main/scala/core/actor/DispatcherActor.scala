package core.actor

import core.datatype.{ReceivedData, ReceivedParticuleData, ReceivedTempData, ReceivedRfidData}
import core.traits.Logging
import org.apache.pekko.actor.{Actor, ActorRef, Props}


object DispatcherActor {

  /**
   * Create Props for a DispatcherActor .
   *
   * @param temperatureActor The actor to which the temperature data will be dispatched.
   * @param particularActor The actor to which the particular data will be dispatched
   * @param rfidActor The actor to which the RFID data will be dispatched
   *
   * @return a Props for creating a DispatcherActor
   */
   def props(temperatureActor: ActorRef, particularActor: ActorRef, rfidActor: ActorRef): Props = Props(new DispatcherActor(temperatureActor,particularActor, rfidActor))
}

class DispatcherActor(temperatureActor: ActorRef, particularActor: ActorRef, rfidActor: ActorRef) extends Actor,Logging {
  def receive: Receive = {
    case Some(d: ReceivedData) => handle(d)
    case other => logger.debug(s"ignored $other")
  }

  /**
   * Dispatch the value to the actor who manage it
   *
   * @param d the data received from other actors
   */
  private def handle(d: ReceivedData): Unit = d match
    case t: ReceivedTempData =>
      temperatureActor ! TemperatureActor.Record(t.temperature)

    case p: ReceivedParticuleData => 
      particularActor ! p

    case r: ReceivedRfidData =>
      rfidActor ! r
}
