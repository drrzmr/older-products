package products.actors

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.eventstream.EventStream
import akka.actor.typed.scaladsl.Behaviors

import products.models.Interval

object ReportsManager {

  sealed trait Command
  final case class EventPartialReport(orderUUID: UUID, reportUUID: UUID, partialReport: Map[Interval, Int])
      extends Command
  final case class GetReport(reportUUID: UUID, replyTo: ActorRef[GetReportResponse]) extends Command
  final case class GetReports(replyTo: ActorRef[GetReportsResponse])                 extends Command
  final case class CreateReport(begin: LocalDateTime,
                                end: LocalDateTime,
                                intervals: Seq[Interval],
                                replyTo: ActorRef[CreateReportResponse])
      extends Command

  final case class CreateReportResponse(reportUUID: UUID)
  final case class GetReportResponse(maybeReport: Option[(UUID, Map[Interval, Int])])
  final case class GetReportsResponse(reportUUIDs: Set[UUID])

  type MyBehavior = Behavior[Command]
  type Ref        = ActorRef[Command]

  def apply(): MyBehavior = Behaviors.setup { ctx =>
    ctx.system.eventStream ! EventStream.Subscribe(ctx.self)
    running(Map.empty)
  }

  def running(table: Map[UUID, Map[Interval, Int]]): MyBehavior =
    Behaviors.receivePartial {

      case (ctx, CreateReport(begin, end, intervals, replyTo)) =>
        val uuid = UUID.randomUUID()
        ctx.system.eventStream ! EventStream.Publish(ReportCluster.GetReport(uuid, begin, end, intervals))
        replyTo ! CreateReportResponse(uuid)
        Behaviors.same

      case (_, GetReport(reportUUID, replyTo)) =>
        replyTo ! GetReportResponse(table.get(reportUUID).map(report => (reportUUID, report)))
        Behaviors.same

      case (_, GetReports(replyTo)) =>
        replyTo ! GetReportsResponse(table.keySet)
        Behaviors.same

      case (ctx, EventPartialReport(orderUUID, reportUUID, partialReport)) =>
        val maybeReport = table.get(reportUUID)
        ctx.log.info(
          s"[evt] reportUUID: $reportUUID, orderUUID: $orderUUID | partialReport: $partialReport, report: $maybeReport")

        maybeReport match {
          case None =>
            val nextReport = partialReport
            val nextTable  = table.updated(reportUUID, nextReport)
            ctx.log.info(
              s"[evt] reportUUID: $reportUUID, orderUUID: $orderUUID | nextReport: $nextReport, nextTable: $nextTable")

            running(table.updated(reportUUID, partialReport))

          case Some(report) =>
            val nextReport = (report.toList ++ partialReport.toList).groupMapReduce(_._1)(_._2)(_ + _)
            val nextTable  = table.updated(reportUUID, nextReport)

            ctx.log.info(
              s"[evt] reportUUID: $reportUUID, orderUUID: $orderUUID | nextReport: $nextReport, nextTable: $nextTable")

            running(nextTable)
        }

    }

}
