package products.http.routes

import java.util.UUID

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import products.actors.ProductsManager
import products.actors.ProductsManager.ActionPerformed
import products.actors.ProductsManager.GetProductResponse
import products.actors.ProductsManager.GetProductsResponse
import products.http.routes.ProductsRoutes._
import products.models.Product
import products.models.api
import products.settings
import products.tools.helpers.now
import products.tools.json.CirceEncoder._

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

object ProductsRoutes {
  case class ProductPayload(name: String, category: String, weight: Long, price: Long) {
    def toProduct: Product = Product(uuid = UUID.randomUUID(), createdAt = now(), name, category, weight, price)
  }
  implicit val decProductPayload: Decoder[ProductPayload] = deriveDecoder
}

class ProductsRoutes(pmRef: ProductsManager.Ref)(implicit val system: ActorSystem[_]) {

  implicit val timeout: Timeout = settings.timeout.actorAsk

  private def getProduct(uuid: UUID)          = pmRef.ask(ProductsManager.GetProduct(uuid, _))
  private def getProducts                     = pmRef.ask(ProductsManager.GetProducts)
  private def createProduct(product: Product) = pmRef.ask(ProductsManager.CreateProduct(product, _))

  val productsRoutes: Route =
    pathPrefix("products") {
      concat(
        (path(JavaUUID) & get & rejectEmptyResponse) { uuid: UUID =>
          onSuccess(getProduct(uuid)) { case GetProductResponse(maybeProduct) => complete(maybeProduct) }

        },
        (pathEndOrSingleSlash & get & rejectEmptyResponse) {
          onSuccess(getProducts) { case GetProductsResponse(products) => complete(products) }
        },
        (pathEndOrSingleSlash & post & entity(as[ProductPayload])) { payload =>
          onSuccess(createProduct(payload.toProduct)) {
            case ActionPerformed(message) => complete(api.Message(message))
          }
        }
      )
    }
}
