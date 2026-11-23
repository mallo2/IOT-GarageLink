package core.traits

import org.slf4j.{Logger, LoggerFactory}

/**
 * Trait to allow logging in actors
 */
trait Logging {
  protected lazy val logger: Logger =
    LoggerFactory.getLogger(getClass)
}