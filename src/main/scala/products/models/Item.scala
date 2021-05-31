package products.models

import java.util.UUID

case class Item(productUUID: UUID, cost: Long, shippingFee: Long, taxAmount: Long)
