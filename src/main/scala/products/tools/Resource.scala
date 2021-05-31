package products.tools

import scala.io.Source
import scala.util.Try

object Resource {

  def getLines(filename: String): Either[Throwable, List[String]] =
    Try(Source.fromInputStream(getClass.getResourceAsStream(s"/$filename")).getLines().toList).toEither
}
