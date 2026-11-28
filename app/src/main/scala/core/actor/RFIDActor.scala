package core.actor

import org.apache.pekko.actor.{Actor, ActorLogging, Props}

object RFIDActor {
  enum ToolState: 
    case In, Out 

  final case class Scan(tag: String, time: String) // etat d'un outil
  final case class Get(tag: String) // etat un outil 
  case object GetAll // etat tt les outils 

  private val DefaultTools = List("hammer", "screwdriver", "wrench")

    /**
   * Create Props for a RFIDActor .
   *
   * @param tools The tags of the tools to track
   *
   * @return a Props for creating a RFIDActor
   */
  def props(tools: List[String] = DefaultTools): Props = Props(new RFIDActor(tools))
}

class RFIDActor(tools: List[String]) extends Actor with ActorLogging {
  import RFIDActor.*

  private var states: Map[String, ToolState] = tools.map(tag => tag -> ToolState.In).toMap

  def receive: Receive = {
    case Scan(tag, time) =>
      states.get(tag) match
        case Some(current) =>
          val next = if current == ToolState.In then ToolState.Out else ToolState.In
          states = states.updated(tag, next)
          log.info("Tool {} : {} -> {} at {}", tag, current, next, time)

        case None =>
          log.warning("Unknown tag {} ignored", tag)

    case Get(tag) => sender() ! states.get(tag)
    case GetAll   => sender() ! states
  }
}
