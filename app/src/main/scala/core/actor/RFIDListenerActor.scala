package core.actor

import com.phidget22.RFID
import org.apache.pekko.actor.{Actor, ActorRef, Props}
import core.traits.Logging
import core.datatype.{ReceivedData, ReceivedRfidData}

import java.time.LocalDateTime


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

  private case object Attached // le sensor est dispo 
  private case object Detached // plus dispo
  private case class TagRead(tag: String) // y a une data
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

  def process_data(rawTag: String): Option[ReceivedData] = {
    val tag = rawTag.trim

    if (tag.isEmpty)
      return None

    Some(ReceivedRfidData(tag = tag, time = LocalDateTime.now()))
  }

  override def postStop(): Unit = reader.close()
}