package products.http.routes

import java.time.LocalDateTime
import java.util.UUID

import scala.concurrent.Future

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import products.actors.ReportsManager
import products.actors.ReportsManager.CreateReport
import products.actors.ReportsManager.CreateReportResponse
import products.actors.ReportsManager.GetReport
import products.actors.ReportsManager.GetReportResponse
import products.actors.ReportsManager.GetReports
import products.actors.ReportsManager.GetReportsResponse
import products.http.routes.ReportsRoutes._
import products.models.Interval
import products.models.api
import products.settings
import products.tools.json.CirceDecoder._
import products.tools.json.CirceEncoder._

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder

object ReportsRoutes {
  case class CreateReportPayload(begin: LocalDateTime, end: LocalDateTime, intervals: Seq[Interval])
  case class ReportItem(interval: Interval, total: Int)
  case class GetReportPayload(reportUUID: UUID, report: Seq[ReportItem])

  implicit val decReportPayload: Decoder[CreateReportPayload] = deriveDecoder
  implicit val encGetReportPayload: Encoder[GetReportPayload] = deriveEncoder
  implicit val encReportItem: Encoder[ReportItem]             = deriveEncoder
}

class ReportsRoutes(ref: ReportsManager.Ref)(implicit val system: ActorSystem[_]) {

  implicit val timeout: Timeout = settings.timeout.actorAsk

  private def getReport(reportUUID: UUID)            = ref.ask(GetReport(reportUUID, _))
  private def getReports: Future[GetReportsResponse] = ref.ask(GetReports)
  private def createReport(begin: LocalDateTime, end: LocalDateTime, intervals: Seq[Interval]) =
    ref.ask(CreateReport(begin, end: LocalDateTime, intervals: Seq[Interval], _))

  val reportsRoutes: Route = pathPrefix("reports") {
    concat(
      (path(JavaUUID) & get & rejectEmptyResponse) { reportUUID =>
        onSuccess(getReport(reportUUID)) {
          case GetReportResponse(maybeReport: Option[(UUID, Map[Interval, Int])]) =>
            complete(maybeReport.map {
              case (reportUUID, report: Map[Interval, Int]) =>
                GetReportPayload(
                  reportUUID = reportUUID,
                  report =
                    report.toList.map { case (interval, total) => ReportItem(interval, total) }.sortBy(_.interval.begin)
                )
            })
        }
      },
      (pathEndOrSingleSlash & get) {
        onSuccess(getReports) {
          case GetReportsResponse(reportUUIDs) =>
            complete(StatusCodes.OK, reportUUIDs)
        }
      },
      (pathEndOrSingleSlash & post & entity(as[CreateReportPayload])) {
        case CreateReportPayload(begin, end, intervals) =>
          onSuccess(createReport(begin, end, intervals)) {
            case CreateReportResponse(reportUUID) =>
              complete(StatusCodes.OK, api.Message(s"report $reportUUID created"))
          }
      },
    )
  }
}
