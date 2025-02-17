package stryker4s.log

import fansi.Str

trait Logger {
  final def debug(msg: => Str): Unit = logImpl(Debug, msg)
  final def debug(msg: => Str, e: Throwable): Unit = logImpl(Debug, msg, e)
  final def debug(e: Throwable): Unit = log(Debug, e)

  final def info(msg: => Str): Unit = logImpl(Info, msg)
  final def info(msg: => Str, e: Throwable): Unit = logImpl(Info, msg, e)
  final def info(e: Throwable): Unit = log(Info, e)

  final def warn(msg: => Str): Unit = logImpl(Warn, msg)
  final def warn(msg: => Str, e: Throwable): Unit = logImpl(Warn, msg, e)
  final def warn(e: Throwable): Unit = log(Warn, e)

  final def error(msg: => Str): Unit = logImpl(Error, msg)
  final def error(msg: => Str, e: Throwable): Unit = logImpl(Error, msg, e)
  final def error(e: Throwable): Unit = log(Error, e)

  final private def logImpl(level: Level, msg: => Str): Unit = log(level, processMsgStr(msg))
  final private def logImpl(level: Level, msg: => Str, e: => Throwable): Unit = log(level, processMsgStr(msg), e)

  def log(level: Level, msg: => String): Unit
  def log(level: Level, msg: => String, e: => Throwable): Unit
  def log(level: Level, e: Throwable): Unit

  /** Process a colored fansi.Str to a String, or plain text if colors are disabled
    */
  @inline private def processMsgStr(msg: fansi.Str): String =
    if (colorEnabled) msg.render else msg.plainText

  /** Whether colors are enabled in the log
    */
  protected val colorEnabled: Boolean = {
    // Explicitly disable color https://no-color.org/
    val notNoColor = !sys.env.contains("NO_COLOR")
    // If there is a TERM on Linux (or Windows Git Bash), assume we support color
    lazy val unixEnabled = sys.env.contains("TERM")
    // On Windows there's no easy way. But if we're in Windows Terminal or ConEmu, we can assume we support color
    lazy val windowsEnabled = sys.env.contains("WT_SESSION") || sys.env.get("ConEmuANSI").contains("ON")

    notNoColor && (unixEnabled || windowsEnabled)
  }
}
