package server.http.controller

import cats.effect.{ContextShift, Sync}
import org.http4s.EntityBody
import sttp.tapir.openapi.Tag
import sttp.tapir.server.ServerEndpoint

abstract class TapirController[F[_]: Sync: ContextShift] {
  def tag: Tag
  def endpoints: IterableOnce[ServerEndpoint[_, _, _, EntityBody[F], F]]
}
