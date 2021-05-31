package products.actors

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.MONTHS
import java.util.UUID

import scala.util.Success

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.eventstream.EventStream
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.util.Timeout

import products.models.Interval
import products.models.Order
import products.settings

object ReportCluster {

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("report")

  def initSharding(productManagerRef: ProductsManager.Ref,
                   system: ActorSystem[_]): ActorRef[ShardingEnvelope[Command]] =
    ClusterSharding(system).init(Entity(TypeKey) { entityContext =>
      ReportCluster(productManagerRef, UUID.fromString(entityContext.entityId))
    })

  implicit val timeout: Timeout = settings.timeout.actorAsk

  sealed trait Command
  final case class CreateOrder(order: Order) extends Command
  final case class GetReport(reportUUID: UUID, begin: LocalDateTime, end: LocalDateTime, intervals: Seq[Interval])
      extends Command

  final case class Data(placedAt: LocalDateTime, productUUID: UUID, productCreatedAt: LocalDateTime) extends Command

  def apply(productManagerRef: ProductsManager.Ref, orderUUID: UUID): Behavior[Command] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"wrapped order id: $orderUUID")
      ctx.system.eventStream ! EventStream.Subscribe(ctx.self)

      def running(list: List[Data]): Behavior[Command] =
        Behaviors.receivePartial {

          case (ctx, CreateOrder(order)) =>
            for (item <- order.items) {
              ctx.ask(productManagerRef, ProductsManager.GetProduct(item.productUUID, _)) {
                case Success(ProductsManager.GetProductResponse(Some(product))) =>
                  Data(order.placedAt, product.uuid, product.createdAt)
                case _ => ???
              }
            }
            Behaviors.same

          case (_, data: Data) => running(data :: list)

          case (_, GetReport(reportUUID, begin, end, intervals)) =>
            /* collect (filter date range and map to (productUUID, months: Long))*/
            val collected: List[(UUID, Long)] = for {
              Data(placedAt, productUUID, productCreatedAt) <- list
              if placedAt.compareTo(begin) >= 0
              if placedAt.compareTo(end) <= 0
              months = MONTHS.between(productCreatedAt, placedAt) + 1
              if months > 0
              days = DAYS.between(productCreatedAt, placedAt)
              if days > 0
            } yield (productUUID, months)

            /* group by interval -> List[ProductUUID, months: Long]*/
            val grouped: Map[Interval, List[(UUID, Long)]] = collected.groupBy {
              case (_, months) =>
                intervals.find {
                  case Interval(begin, end) => months >= begin && (months <= end || end == -1)
                }.getOrElse(Interval(-1, -1))
            }

            /* compute elements into each interval */
            val report: Map[Interval, Int] = grouped.view.mapValues(_.length).toMap

            ctx.system.eventStream ! EventStream.Publish(
              ReportsManager.EventPartialReport(orderUUID, reportUUID, report))

            Behaviors.same
        }

      running(List.empty)
    }

}
