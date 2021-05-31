package products.actors

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.http.scaladsl.server.Directives.concat
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.util.Timeout

import products.actors.ReportCluster.initSharding
import products.http.Server
import products.http.routes._
import products.settings

object Guardian {

  implicit val timeout: Timeout = settings.timeout.actorAsk

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val system: ActorSystem[Nothing] = ctx.system

    val productsManagerRef = ctx.spawn(ProductsManager(), "product-manager")
    ctx.watch(productsManagerRef)

    val ordersManagerRef = ctx.spawn(OrdersManager(), "order-manager")
    ctx.watch(ordersManagerRef)

    val reportsManagerRef = ctx.spawn(ReportsManager(), "reports-manager")
    ctx.watch(reportsManagerRef)

    val clusterRef: ActorRef[ShardingEnvelope[ReportCluster.Command]] = initSharding(productsManagerRef, system)
    ctx.watch(clusterRef)

    Server.start(
      concat(
        new FrontDoorRoutes().frontDoorRoutes,
        pathPrefix("v1") {
          concat(
            new ProductsRoutes(productsManagerRef).productsRoutes,
            new OrdersRoutes(ordersManagerRef).ordersRoutes,
            pathPrefix("_") {
              concat(
                new ImporterRoutes(productsManagerRef, ordersManagerRef).importerRoutes,
                new ReportsRoutes(reportsManagerRef).reportsRoutes
              )
            }
          )
        }
      )
    )

    Behaviors.empty
  }

}
