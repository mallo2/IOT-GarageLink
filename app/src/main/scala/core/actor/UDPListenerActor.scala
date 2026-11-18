package core.actor

import org.apache.pekko.actor.{Actor, ActorRef, Props}
import org.apache.pekko.io.Udp
import org.apache.pekko.io.IO

import core.traits.Logging

import java.net.InetSocketAddress

object UDPListenerActor {

  /**
   * Create Props for a UDPListenerActor .
   *
   * @param nextActor  The actor to which the data will be transmitted
   * @param ip  The ip which will be listened
   * @param port  The port which will be listened
   *
   * @return a Props for creating a UDPListenerActor
   */
  def props(nextActor:ActorRef,ip:String,port:Int): Props = Props(new UDPListenerActor(nextActor, ip, port))
}

class UDPListenerActor(nextActor: ActorRef, ip: String, port: Int) extends Actor, Logging {
  import context.system
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(ip, port)) // IP,PORT

  def receive: Receive = {
    case Udp.Bound(local) =>
      logger.info(s"UDP listener successfully bound on $local")
      context.become(ready())
  }

  def ready(): Receive = {
    case Udp.Received(data, remote) =>
      val processed = data.utf8String // Here we should process data
      logger.info(s"UDP data received $processed")
      nextActor ! processed
    // I deleted unbound instruction, we can see if we need it
  }
}