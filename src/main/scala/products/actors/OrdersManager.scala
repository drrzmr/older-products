package products.actors

import java.util.UUID

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding

import products.models.Order

object OrdersManager {
  sealed trait Command
  final case class GetOrder(uuid: UUID, replyTo: ActorRef[GetOrderResponse])     extends Command
  final case class GetOrders(replyTo: ActorRef[GetOrdersResponse])               extends Command
  final case class CreateOrder(order: Order, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class ExportOrders(replyTo: ActorRef[ExportOrders])                 extends Command

  final case class GetOrderResponse(maybeOrder: Option[Order])
  final case class GetOrdersResponse(orders: Seq[Order])
  final case class ActionPerformed(message: String)

  type Ref        = ActorRef[Command]
  type MyBehavior = Behavior[Command]

  def apply(): MyBehavior =
    Behaviors.setup { ctx: ActorContext[Command] =>
      running(Map.empty, ClusterSharding(ctx.system))
    }

  def running(table: Map[UUID, Order], sharding: ClusterSharding): MyBehavior =
    Behaviors.receivePartial {

      case (_, CreateOrder(order, replyTo)) =>
        val next      = table.updated(order.orderUUID, order)
        val entityRef = sharding.entityRefFor(ReportCluster.TypeKey, order.orderUUID.toString)

        replyTo ! ActionPerformed(s"order ${order.orderUUID} created")
        entityRef ! ReportCluster.CreateOrder(order)

        running(next, sharding)

      case (_, GetOrder(uuid, replyTo)) =>
        replyTo ! GetOrderResponse(table.get(uuid))
        Behaviors.same

      case (_, GetOrders(replyTo)) =>
        replyTo ! GetOrdersResponse(table.values.toList)
        Behaviors.same
    }

}
