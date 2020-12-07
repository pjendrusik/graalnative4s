package server.http.controller

import cats.Applicative
import cats.effect.{ContextShift, Sync}
import cats.implicits._
import io.circe.generic.auto._
import server.model.UserProfile
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.openapi.Tag
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{endpoint, statusCode}

import java.time.LocalDateTime

class UserController[F[_]: Sync: ContextShift](implicit F: Applicative[F]) extends TapirController {

  val tag: Tag = Tag("User profile", None)

  lazy val endpoints = List(index)

  def index: ServerEndpoint[Unit, StatusCode, UserProfile, Nothing, F] =
    endpoint.get
      .summary("Get user profile")
      .tag(tag.name)
      .in("user")
      .out(jsonBody[UserProfile])
      .errorOut(statusCode)
      .serverLogic(_ =>
        F.pure(
          UserProfile(
            "pawel",
            LocalDateTime.now()
        ).asRight))

}
