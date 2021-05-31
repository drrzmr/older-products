package products

import akka.actor.typed.ActorSystem

import products.actors.Guardian

object HttpServerApp extends App {

  val system: ActorSystem[Nothing] = ActorSystem[Nothing](Guardian(), "orders")
}
