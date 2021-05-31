package products.actors

import java.util.UUID

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import products.models.Product

object ProductsManager {
  sealed trait Command
  final case class CreateProduct(product: Product, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetProduct(uuid: UUID, replyTo: ActorRef[GetProductResponse])       extends Command
  final case class GetProducts(replyTo: ActorRef[GetProductsResponse])                 extends Command

  final case class GetProductResponse(maybeProduct: Option[Product])
  final case class GetProductsResponse(products: Seq[Product])
  final case class ActionPerformed(message: String)

  type Ref        = ActorRef[Command]
  type MyBehavior = Behavior[Command]

  def apply(): MyBehavior = running(Map.empty)

  def running(table: Map[UUID, Product]): MyBehavior =
    Behaviors.receive { (_, msg) =>
      msg match {
        case CreateProduct(product, replyTo) =>
          replyTo ! ActionPerformed(s"product ${product.uuid} created")
          running(table.updated(product.uuid, product))

        case GetProduct(uuid, replyTo) =>
          replyTo ! GetProductResponse(table.get(uuid))
          Behaviors.same

        case GetProducts(replyTo) =>
          replyTo ! GetProductsResponse(table.values.toList)
          Behaviors.same
      }

    }

}
