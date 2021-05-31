package products.tools

import java.time.LocalDateTime
import java.util.UUID

import scala.annotation.tailrec
import scala.io.Source
import scala.util.Try

object helpers {
  def ldt(str: String): Either[Throwable, LocalDateTime]               = DateTime.createLocalDateTime(str)
  def now(): LocalDateTime                                             = LocalDateTime.now()
  def resourceLines(filename: String): Either[Throwable, List[String]] = Resource.getLines(filename)

  object UUID {
    def random: UUID                   = java.util.UUID.randomUUID
    def fromString(uuid: String): UUID = java.util.UUID.fromString(uuid)
  }

  def forEachLine(fn: String)(lineHandler: String => Unit): Try[Long] = {

    @tailrec
    def iterate(it: Iterator[String], counter: Long = 0): Long =
      it.nextOption() match {
        case None => counter
        case Some(line) =>
          lineHandler(line)
          iterate(it, counter + 1)
      }

    Try {
      val bufferedSource = Source.fromFile(fn)
      try {
        iterate(bufferedSource.getLines())
      } finally {
        bufferedSource.close()
      }
    }
  }

}
