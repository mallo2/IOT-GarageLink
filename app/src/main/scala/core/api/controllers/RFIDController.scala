package core.api.controllers

import core.actor.RFIDActor
import core.traits.ApiController
import io.circe.generic.auto.*
import org.apache.pekko.actor.ActorRef
import org.apache.pekko.pattern.ask
import org.apache.pekko.util.Timeout
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}

final case class ToolStatus(tag: String, state: String)

class RFIDController(rfidActor: ActorRef)(using ExecutionContext) extends ApiController:
  private given Timeout = Timeout(3.seconds)

  private val base = endpoint.get.in("rfid")

  private val allTools =
    base
      .out(jsonBody[List[ToolStatus]])
      .serverLogicSuccess[Future] { _ =>
        (rfidActor ? RFIDActor.GetAll)
          .mapTo[Map[String, RFIDActor.ToolState]]
          .map(_.toList.map((tag, state) => ToolStatus(tag, state.toString)))
      }

  private val tool =
    base
      .in(path[String]("tag"))
      .out(jsonBody[ToolStatus])
      .errorOut(statusCode(StatusCode.NotFound))
      .serverLogic[Future] { tag =>
        (rfidActor ? RFIDActor.Get(tag))
          .mapTo[Option[RFIDActor.ToolState]]
          .map(_.map(state => ToolStatus(tag, state.toString)).toRight(()))
      }

  val endpoints: List[ServerEndpoint[Any, Future]] = List(allTools, tool)