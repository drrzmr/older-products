package products.tools

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.util.Try

import products.settings

object DateTime {

  private val formatter         = Try(DateTimeFormatter.ofPattern(settings.dateTimePattern)).toEither
  private val dateTimeFormatter = DateTimeFormatter.ofPattern(settings.dateTimePattern)

  def createLocalDateTime(str: String): Either[Throwable, LocalDateTime] =
    for {
      fmt <- formatter
      ldt <- Try(LocalDateTime.parse(str, fmt)).toEither
    } yield ldt

  implicit class RichLocalDateTime(private val value: LocalDateTime) extends AnyVal {
    def str: String = value.format(dateTimeFormatter)
  }
}
