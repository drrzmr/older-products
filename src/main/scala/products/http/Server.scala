package products.http

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import products.settings

object Server {

  def start(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val futureBinding: Future[Http.ServerBinding] = Http()
      .newServerAt(interface = settings.http.host, port = settings.http.port)
      .bind(routes)

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"Listening on: http://${address.getHostString}:${address.getPort}")
      case Failure(exception) =>
        println(s"Binding on http://${settings.http.host}:${settings.http.port} failed")
        system.log.error(s"Binding on http://${settings.http.host}:${settings.http.port} failed", exception)
        system.terminate()
    }

  }

}
