package server.http.controller

import cats.Applicative
import cats.effect.{ContextShift, Sync}
import cats.implicits._
import dev.pjendrusik.BuildInfo
import server.model.Info
import sttp.model.StatusCode
import io.circe.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{endpoint, query, statusCode, stringBody}
import sttp.tapir.openapi.Tag

class DefaultController[F[_]: Sync: ContextShift](implicit F: Applicative[F]) extends TapirController {

  val tag: Tag = Tag("Getting started", None)

  lazy val endpoints = List(hello, info)

  def hello: ServerEndpoint[Option[String], StatusCode, String, Nothing, F] =
    endpoint.get
      .summary("The infamous hello world endpoint")
      .tag(tag.name)
      .in("hello")
      .in(query[Option[String]]("name").description("Optional name to greet"))
      .out(stringBody)
      .errorOut(statusCode)
      .serverLogic(name =>
        F.pure(
          s"Hello ${name.getOrElse("World")}!"
            .asRight))

  def info: ServerEndpoint[Unit, StatusCode, Info, Nothing, F] =
    endpoint.get
      .summary("Fetch general information about the application")
      .tag(tag.name)
      .in("info")
      .out(jsonBody[Info])
      .errorOut(statusCode)
      .serverLogic(_ =>
        F.pure(
          Info(
            BuildInfo.name,
            BuildInfo.version,
            BuildInfo.builtAtString,
            "/docs"
          ).asRight
        )
      )
}
