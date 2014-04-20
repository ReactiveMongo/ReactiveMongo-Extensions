package reactivemongo.extensions.util

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.pattern.ClassicConverter

/** A logback converter generating colored, lower-case level names.
  *
  * Used for example as:
  * {{{
  * %coloredLevel %logger{15} - %message%n%xException{5}
  * }}}
  */
class ColoredLevel extends ClassicConverter {

  def convert(event: ILoggingEvent): String = {
    event.getLevel match {
      case Level.TRACE => "[" + Colors.blue("trace") + "]"
      case Level.DEBUG => "[" + Colors.cyan("debug") + "]"
      case Level.INFO => "[" + Colors.white("info") + "]"
      case Level.WARN => "[" + Colors.yellow("warn") + "]"
      case Level.ERROR => "[" + Colors.red("error") + "]"
    }
  }

}
