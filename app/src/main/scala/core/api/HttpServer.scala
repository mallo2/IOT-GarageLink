package core.api

import core.traits.ApiController
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import sttp.tapir.server.interceptor.cors.{CORSConfig, CORSInterceptor}
import sttp.tapir.server.pekkohttp.{PekkoHttpServerInterpreter, PekkoHttpServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.{ExecutionContext, Future}

object HttpServer:

  def start(controllers: List[ApiController], host: String, port: Int, allowedOrigins: List[String])(using system: ActorSystem[?], ec: ExecutionContext): Future[Http.ServerBinding] =
    val corsConfig =
      if allowedOrigins.isEmpty then CORSConfig.default.allowAllOrigins
      else CORSConfig.default.allowMatchingOrigins(allowedOrigins.toSet.contains)

    val serverOptions: PekkoHttpServerOptions =
      PekkoHttpServerOptions.customiseInterceptors
        .corsInterceptor(CORSInterceptor.customOrThrow[Future](corsConfig))
        .options

    val apiEndpoints = controllers.flatMap(_.endpoints)
    val docsEndpoints = SwaggerInterpreter().fromServerEndpoints[Future](apiEndpoints, "IoT API", "1.0")

    val route = PekkoHttpServerInterpreter(serverOptions).toRoute(apiEndpoints ++ docsEndpoints)

    Http().newServerAt(host, port).bind(route)