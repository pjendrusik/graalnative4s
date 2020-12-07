package server

import cats.data.Kleisli
import cats.effect.{ContextShift, Sync}
import cats.implicits._
import dev.pjendrusik.BuildInfo
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.implicits._
import org.http4s.server.middleware.CORS
import sttp.tapir._
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.{OpenAPI, Server}
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.swagger.http4s.SwaggerHttp4s

object Api {
  def apply[F[_]: Sync: ContextShift](config: ApiDocsConfiguration): Kleisli[F, Request[F], Response[F]] = {

    val dsl = Http4sDsl[F]
    import dsl._

    val router: TapirRouter[F] = TapirRouter()

    val docs: OpenAPI = router.endpoints
      .toOpenAPI(openapi.Info(BuildInfo.name, BuildInfo.version, config.description))
      .servers(List(Server(config.serverUrl)))
      .tags(router.serverTags)

    val redirectRootToHello = HttpRoutes.of[F] { case path @ GET -> Root => PermanentRedirect(Location(path.uri / "hello")) }

    val swaggerRoutes = List(redirectRootToHello, new SwaggerHttp4s(docs.toYaml).routes)

    val routes: List[HttpRoutes[F]] = List(router.routes) ++ swaggerRoutes

    CORS(routes.reduce(_ <+> _)).orNotFound
  }
}
