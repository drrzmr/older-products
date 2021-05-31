package products.tools.json

import java.time.LocalDateTime
import java.util.UUID

import products.models.Interval
import products.models.Item
import products.models.Order
import products.models.Product
import products.tools.helpers.ldt

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

object CirceDecoder {
  implicit val decProduct: Decoder[Product] = deriveDecoder
  implicit val decOrder: Decoder[Order]     = deriveDecoder
  implicit val decItem: Decoder[Item]       = deriveDecoder

  implicit val decInterval: Decoder[Interval]           = deriveDecoder
  implicit val decUUID: Decoder[UUID]                   = Decoder.decodeUUID
  implicit val decLocalDateTime: Decoder[LocalDateTime] = Decoder.decodeString.emapTry(str => ldt(str).toTry)
}
