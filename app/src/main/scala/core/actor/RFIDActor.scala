package core.actor

import org.apache.pekko.actor.{Actor, ActorLogging, Props}

object RFIDActor {
  enum ToolState: 
    case In, Out 

  final case class Scan(tag: String, time: String) 
  final case class Get(tag: String) 
  case object GetAll 

  private val MaxTools = 3

    /**
   * Create Props for a RFIDActor .
   *
   * @return a Props for creating a RFIDActor
   */
  def props(): Props = Props(new RFIDActor)
}

class RFIDActor extends Actor with ActorLogging {
  import RFIDActor.*

  private var states = Map.empty[String, ToolState]

  def receive: Receive = {
    case Scan(tag, time) =>
      val isNew = !states.contains(tag)

      if (isNew && states.size >= MaxTools)
        log.warning("Tool {} ignored: max number of tools ({}) reached", tag, MaxTools)
      else {
        val current = states.getOrElse(tag, ToolState.Out)
        val next    = if current == ToolState.In then ToolState.Out else ToolState.In
        states = states.updated(tag, next)
        log.info("Tool {} : {} -> {} at {}", tag, current, next, time)
      }

    case Get(tag) => sender() ! states.get(tag)
    case GetAll   => sender() ! states
  }
}
