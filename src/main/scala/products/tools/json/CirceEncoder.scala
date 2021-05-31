package products.tools.json

import java.time.LocalDateTime
import java.util.UUID

import products.models._
import products.tools.DateTime._

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

object CirceEncoder {
  implicit val encUUID: Encoder[UUID]                   = Encoder.encodeUUID
  implicit val encLocalDateTime: Encoder[LocalDateTime] = Encoder.encodeString.contramap[LocalDateTime](_.str)
  implicit val encInterval: Encoder[Interval]           = deriveEncoder

  implicit val encProduct: Encoder[Product] = deriveEncoder
  implicit val encItem: Encoder[Item]       = deriveEncoder
  implicit val encOrder: Encoder[Order]     = deriveEncoder

  implicit val encApiMessage: Encoder[api.Message] = deriveEncoder
}
