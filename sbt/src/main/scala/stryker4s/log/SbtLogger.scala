package stryker4s.log

import sbt.{Level as SbtLevel, Logger as SbtInternalLogger}

class SbtLogger(sbtLogger: SbtInternalLogger) extends Logger {

  def log(level: Level, msg: => String): Unit = sbtLogger.log(toSbtLevel(level), msg)

  def log(level: Level, msg: => String, e: => Throwable): Unit = {
    sbtLogger.log(toSbtLevel(level), msg)
    sbtLogger.trace(e)
  }

  def log(level: Level, e: Throwable): Unit = sbtLogger.trace(e)

  private def toSbtLevel(level: Level): SbtLevel.Value = level match {
    case Debug => SbtLevel.Debug
    case Info  => SbtLevel.Info
    case Warn  => SbtLevel.Warn
    case Error => SbtLevel.Error
  }

  override protected val colorEnabled: Boolean =
    sbt.internal.util.Terminal.console.isColorEnabled && !sys.env.contains("NO_COLOR")
}
