package core.Actor

import org.apache.pekko.actor.{Actor, Props}


object TemperatureActor {
  /**
   * Create Props for a TemperatureActor .
   *
   * @return a Props for creating a TemperatureActor
   */
  def props(): Props = Props(new TemperatureActor)
}

class TemperatureActor extends Actor {
  def receive: Receive = {
    case v  => println(s"Temps value $v")
  }
}
