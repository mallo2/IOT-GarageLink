
import com.phidget22.{
  VoltageRatioInput,
  VoltageRatioInputVoltageRatioChangeListener,
  VoltageRatioInputVoltageRatioChangeEvent
}

object MicroLoadCell {

  val poidsReference = 150.0

  var zeroRatio = 0.0
  var calibrationFactor = 0.0

  // Attend réellement une pression sur ENTREE
  def attendreEntree(message: String): Unit = {
    println("\n" + message)
    println(">>> APPUIE SUR ENTREE POUR CONTINUER <<<")

    var caractere = 0

    // Ignore les caractères jusqu'à ENTREE
    while (caractere != 10 && caractere != 13) {
      caractere = System.in.read()

      if (caractere == -1) {
        throw new Exception("Entrée clavier indisponible.")
      }
    }
  }

  def main(args: Array[String]): Unit = {

    val sensor = new VoltageRatioInput()

    try {
      sensor.setChannel(0)
      sensor.open(10000)

      println("Phidget connecte !")

      sensor.setDataInterval(100)
      sensor.setVoltageRatioChangeTrigger(0)
      sensor.setBridgeEnabled(true)

      // ETAPE 1 : ZERO
      attendreEntree("Retire tout poids du capteur.")

      Thread.sleep(1000)
      zeroRatio = sensor.getVoltageRatio()

      println(f"Zero enregistre : $zeroRatio%.8f")

      // ETAPE 2 : REFERENCE
      attendreEntree(s"Place $poidsReference grammes sur le capteur.")

      Thread.sleep(1000)
      val referenceRatio = sensor.getVoltageRatio()

      val difference = referenceRatio - zeroRatio

      if (math.abs(difference) < 1e-10) {
        throw new Exception("Signal trop faible pour calibrer.")
      }

      calibrationFactor = poidsReference / difference

      println("\nCALIBRAGE TERMINE !")
      println(f"Facteur : $calibrationFactor%.2f")

      // ETAPE 3 : MESURE PERMANENTE
      sensor.addVoltageRatioChangeListener(
        new VoltageRatioInputVoltageRatioChangeListener {

          override def onVoltageRatioChange(
                                             event: VoltageRatioInputVoltageRatioChangeEvent
                                           ): Unit = {

            val ratio = event.getVoltageRatio()
            val poids = (ratio - zeroRatio) * calibrationFactor

            print(f"\rPoids : $poids%.2f g       ")
          }
        }
      )

      while (true) {
        Thread.sleep(1000)
      }

    } catch {
      case e: Exception =>
        println("\nErreur : " + e.getMessage)

    } finally {
      sensor.close()
    }
  }
}
