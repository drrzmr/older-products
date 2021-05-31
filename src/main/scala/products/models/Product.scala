package products.models

import java.time.LocalDateTime
import java.util.UUID

case class Product(uuid: UUID, createdAt: LocalDateTime, name: String, category: String, weight: Long, price: Long)
