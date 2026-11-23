package core.actor.datagetter


import core.datatype.{ReceivedData, ReceivedParticuleData, ReceivedTempData}
import core.traits.Logging
import org.apache.pekko.actor.{Actor, ActorRef, Props}
import org.apache.pekko.io.{IO, Udp}

import java.net.InetSocketAddress
import java.time.LocalTime
import scala.util.Try

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
      val processed = checkAndFormatData(data.utf8String)
      nextActor ! processed
  }

  /**
   * Check whether the rawdata received respect the expected format (sensorName;Values;HH:mm:ss) and transform it into a new one
   *
   * @param rawData The data received from udp port in a raw format
   *
   * @return Some(data) if the rawdata received respect the expected format, None otherwise
   */
  private def checkAndFormatData(rawData: String): Option[ReceivedData] = {
    val splittedData = rawData.split(";", -1)

    if (splittedData.length != 3)
      return None

    val sensorName = splittedData(0).trim
    val rawValues = splittedData(1).split(",", -1).map(_.trim)
    val timeStamp = splittedData(2).trim

    if (sensorName.isEmpty || rawValues.isEmpty || timeStamp.isEmpty)
      return None

    val values = rawValues.map(v => Try(v.toDouble).toOption)
    val flattenValues = values.flatten.toList

    if (Try(LocalTime.parse(timeStamp)).isFailure)
      return None

    sensorName.match {
      case "pms5003" =>
        if (values.length == 3) {
          Some(
            ReceivedParticuleData(
              id = sensorName,
              ufp = flattenValues(0),
              fp = flattenValues(1),
              p = flattenValues(2),
              time = timeStamp
            )
          )
        } else {
          None
        }

      case "dht11" =>
        if (values.length == 2) {
          Some(
            ReceivedTempData(
              id = sensorName,
              temperature = flattenValues(0),
              humidity = flattenValues(1),
              time = timeStamp
            )
          )
        } else {
          None
        }

      case _ => None
    }
  }
}