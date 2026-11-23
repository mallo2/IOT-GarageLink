package core.traits

import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

/**
 * Traits to enforce controller to declared their endpoints
 */
trait ApiController:
  def endpoints: List[ServerEndpoint[Any, Future]]
