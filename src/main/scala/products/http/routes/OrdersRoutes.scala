package products.http.routes

import java.util.UUID

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import products.actors.OrdersManager
import products.models.Item
import products.models.Order
import products.models.api
import products.settings
import products.tools.helpers.now
import products.tools.json.CirceDecoder._
import products.tools.json.CirceEncoder._

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

object OrdersRoutes {
  case class OrderPayload(customerName: String,
                          contact: String,
                          shippingAddress: String,
                          grandTotal: Int,
                          items: Seq[Item]) {

    def toOrder: Order =
      Order(orderUUID = UUID.randomUUID(), placedAt = now(), customerName, contact, shippingAddress, grandTotal, items)
  }

  implicit val decOrderPayload: Decoder[OrderPayload] = deriveDecoder
}

class OrdersRoutes(omRef: OrdersManager.Ref)(implicit val system: ActorSystem[_]) {

  import OrdersRoutes._

  implicit val timeout: Timeout = settings.timeout.actorAsk

  private def getOrder(orderUUID: UUID) = omRef.ask(OrdersManager.GetOrder(orderUUID, _))
  private def getOrders                 = omRef.ask(OrdersManager.GetOrders)
  private def createOrder(order: Order) = omRef.ask(OrdersManager.CreateOrder(order, _))

  val ordersRoutes: Route =
    pathPrefix("orders") {
      concat(
        (path(JavaUUID) & get & rejectEmptyResponse) { orderUUID: UUID =>
          onSuccess(getOrder(orderUUID)) { case OrdersManager.GetOrderResponse(maybeOrder) => complete(maybeOrder) }
        },
        (pathEndOrSingleSlash & get & rejectEmptyResponse) {
          onSuccess(getOrders) { case OrdersManager.GetOrdersResponse(orders) => complete(orders) }
        },
        (pathEndOrSingleSlash & post & entity(as[OrderPayload])) { orderPayload =>
          onSuccess(createOrder(orderPayload.toOrder)) {
            case OrdersManager.ActionPerformed(message) => complete(api.Message(message))
          }
        },
      )
    }
}
