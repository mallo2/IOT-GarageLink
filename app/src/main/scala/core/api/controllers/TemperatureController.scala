package core.api.controllers

import core.actor.TemperatureActor
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

final case class TemperatureValue(value: Double)
final case class TemperatureHistory(values: Vector[Double])

class TemperatureController(temperatureActor: ActorRef)(using ExecutionContext) extends ApiController:
  private given Timeout = Timeout(3.seconds)

  private val base = endpoint.get.in("temperature")

  private val current =
    base
      .out(jsonBody[TemperatureValue])
      .errorOut(statusCode(StatusCode.NotFound))
      .serverLogic[Future] { _ =>
        (temperatureActor ? TemperatureActor.Get)
          .mapTo[Option[Double]]
          .map(_.map(TemperatureValue.apply).toRight(()))
      }

  private val history =
    base
      .in("history")
      .out(jsonBody[TemperatureHistory])
      .serverLogicSuccess[Future] { _ =>
        (temperatureActor ? TemperatureActor.GetAll)
          .mapTo[Vector[Double]]
          .map(TemperatureHistory.apply)
      }

  val endpoints: List[ServerEndpoint[Any, Future]] = List(current, history)