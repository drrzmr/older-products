package products.http.routes

import scala.concurrent.Future

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import products.actors.OrdersManager
import products.actors.ProductsManager
import products.models.Order
import products.models.Product
import products.models.api
import products.settings
import products.tools.json.CirceDecoder._
import products.tools.json.CirceEncoder._

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

class ImporterRoutes(productsManagerRef: ProductsManager.Ref, ordersManagerRef: OrdersManager.Ref)(
    implicit system: ActorSystem[_]) {

  implicit val timeout: Timeout = settings.timeout.actorAsk

  private def createProduct(product: Product): Future[ProductsManager.ActionPerformed] =
    productsManagerRef.ask(ProductsManager.CreateProduct(product, _))

  private def createOrder(order: Order): Future[OrdersManager.ActionPerformed] =
    ordersManagerRef.ask(OrdersManager.CreateOrder(order, _))

  val importerRoutes: Route = pathPrefix("importer") {
    concat(
      (pathPrefix("products") & pathEndOrSingleSlash & post & entity(as[Product])) { product =>
        onSuccess(createProduct(product)) {
          case ProductsManager.ActionPerformed(message) => complete(StatusCodes.OK, api.Message(message))
        }
      },
      (pathPrefix("orders") & pathEndOrSingleSlash & post & entity(as[Order])) { order =>
        onSuccess(createOrder(order)) {
          case OrdersManager.ActionPerformed(message) => complete(StatusCodes.OK, api.Message(message))
        }
      }
    )
  }

}
