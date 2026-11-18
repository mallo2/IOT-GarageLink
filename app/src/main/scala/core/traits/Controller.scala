package core.traits

import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait ApiController:
  def endpoints: List[ServerEndpoint[Any, Future]]
