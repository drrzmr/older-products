package products.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import products.models.api
import products.tools.json.CirceEncoder._

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

class FrontDoorRoutes {

  val frontDoorRoutes: Route =
    (pathEndOrSingleSlash & get) {
      complete(StatusCodes.OK, api.Message("welcome to order service"))
    }
}
