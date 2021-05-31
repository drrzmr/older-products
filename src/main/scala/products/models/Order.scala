package products.models

import java.time.LocalDateTime
import java.util.UUID

case class Order(orderUUID: UUID,
                 placedAt: LocalDateTime,
                 customerName: String,
                 contact: String,
                 shippingAddress: String,
                 grandTotal: Long,
                 items: Seq[Item])
