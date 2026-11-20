package core.actor

import org.apache.pekko.actor.{Actor, Props}

object RFIDActor {
    /**
   * Create Props for a RFIDActor .
   *
   * @return a Props for creating a RFIDActor
   */
  def props(): Props = Props(new RFIDActor)
}

class RFIDActor extends Actor {
  def receive: Receive = {
    case r  => println(s"rfid value $r")
  }
}