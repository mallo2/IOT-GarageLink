package core.config

import io.github.cdimascio.dotenv.Dotenv

object AppConfig {

  private val dotenv = Dotenv.configure()
    .directory(".")
    .ignoreIfMissing()
    .load()

  val udpHost: String =
    dotenv.get("UDP_HOST", "0.0.0.0")

  val udpPort: Int =
    dotenv.get("UDP_PORT", "5000").toInt

}
