package core.actor

import org.apache.pekko.actor.{Actor, ActorLogging, Props}

object TemperatureActor {
  final case class Record(value: Double)
  case object Get
  case object GetAll

  private val MaxHistory = 1000

  def props(): Props = Props(new TemperatureActor)
}

class TemperatureActor extends Actor with ActorLogging {
  import TemperatureActor.*

  private var history = Vector.empty[Double]

  def receive: Receive = {
    case Record(v) =>
      history = (history :+ v).takeRight(MaxHistory)
      log.info("Temps value {}", v)

    case Get    => sender() ! history.lastOption
    case GetAll => sender() ! history
  }
}