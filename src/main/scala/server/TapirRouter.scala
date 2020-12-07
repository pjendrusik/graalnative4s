package server

import cats.effect.{ContextShift, Sync}
import sttp.tapir.server.http4s._
import org.http4s.{EntityBody, HttpRoutes}
import server.http.controller.{DefaultController, TapirController, UserController}
import sttp.tapir.Endpoint
import sttp.tapir.openapi.Tag
import sttp.tapir.server.ServerEndpoint

abstract class TapirRouter[F[_]: Sync: ContextShift] {
  def serverTags: List[Tag]
  def serverControllers: List[TapirController[F]]
  def serverEndpoints: List[ServerEndpoint[_, _, _, EntityBody[F], F]]
  def endpoints: List[Endpoint[_, _, _, _]] = serverEndpoints.map(_.endpoint)
  def routes: HttpRoutes[F]                 = serverEndpoints.toRoutes
}

object TapirRouter {
  def apply[F[_]: Sync: ContextShift](): TapirRouter[F] = new TapirRouter[F] {

    private val defaultController = new DefaultController[F]()
    private val userController = new UserController[F]()
    private val controllers: List[TapirController[F]] = List(defaultController, userController)

    override def serverControllers: List[TapirController[F]] =
      controllers

    override def serverEndpoints: List[ServerEndpoint[_, _, _, EntityBody[F], F]] =
      controllers.flatMap(_.endpoints)

    override def serverTags: List[Tag] =
      controllers.map(_.tag)

  }
}

