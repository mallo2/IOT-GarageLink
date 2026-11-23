package core.actor.datagetter

import com.phidget22.RFID
import core.actor.datagetter.RFIDListenerActor
import core.datatype.{ReceivedData, ReceivedRfidData}
import core.traits.Logging
import org.apache.pekko.actor.{Actor, ActorRef, Props}

import java.time.LocalTime
import java.time.format.DateTimeFormatter


object RFIDListenerActor {

  /**
   * Create Props for a RFIDListenerActor .
   *
   * @param nextActor  The actor to which the data will be transmitted
   *
   * @return a Props for creating a RFIDListenerActor
   */
  def props(nextActor: ActorRef): Props = Props(new RFIDListenerActor(nextActor))
}

class RFIDListenerActor(nextActor: ActorRef) extends Actor, Logging {

  private case object Attached 
  private case object Detached 
  private case class TagRead(tag: String)
  private val reader = new RFID()

  reader.addAttachListener(_ => self ! Attached)
  reader.addDetachListener(_ => self ! Detached)
  reader.addTagListener(e => self ! TagRead(e.getTag))
  reader.open()

  def receive: Receive = {
    case Attached =>
      logger.info("RFID reader successfully attached")
      context.become(ready())
  }

  def ready(): Receive = {
    case TagRead(tag) =>
      val processed = process_data(tag)
      nextActor ! processed

    case Detached =>
      logger.warn("RFID reader detached, waiting for it to come back")
      context.become(receive) 
  }


  /**
   * Check whether the rawTag received respect the expected format and transform it into a new one
   *
   * @param rawTag the raw tag received from the RFID phidget
   *
   * @return Some(data) if the rawTag respect the expected format, None otherwise
   */
  private def process_data(rawTag: String): Option[ReceivedData] = {
    val tag = rawTag.trim

    if (tag.isEmpty)
      return None

    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val rfidtime = LocalTime.now()
    val formattedTime = rfidtime.format(formatter) 

    Some(ReceivedRfidData(tag = tag, time = formattedTime))
  }
}