package core.Actor

import org.apache.pekko.actor.{Actor, Props}


object ParticularActor {
  /**
   * Create Props for a ParticularActor .
   *
   * @return a Props for creating a ParticularActor
   */
   def props(): Props = Props(new ParticularActor)
}

class ParticularActor extends Actor {
  def receive: Receive = {
    case v  => println(s"Particule value $v")
  }
}
