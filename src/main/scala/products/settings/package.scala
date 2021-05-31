package products

import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration

package object settings {
  val dateTimePattern = "yyyy-MM-dd HH:mm:ss"

  object timeout {
    val actorAsk: FiniteDuration = 3.seconds
  }

  object http {
    val host = "localhost"
    val port = 8080
  }
}
