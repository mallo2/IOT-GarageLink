package core

import com.phidget22.RFID
import com.phidget22.RFIDTagListener
import com.phidget22.RFIDTagLostListener
import com.phidget22.RFIDTagEvent
import com.phidget22.RFIDTagLostEvent
import com.phidget22.PhidgetException

object RFIDReader {

  def main(args: Array[String]): Unit = {

    val rfid = new RFID()

    // Détection d'un badge
    rfid.addTagListener(new RFIDTagListener {
      override def onTag(event: RFIDTagEvent): Unit = {
        println(s"Badge détecté : ${event.getTag()}")
      }
    })

    // Retrait d'un badge
    rfid.addTagLostListener(new RFIDTagLostListener {
      override def onTagLost(event: RFIDTagLostEvent): Unit = {
        println(s"Badge retiré : ${event.getTag()}")
      }
    })

    try {
      // Connexion au lecteur USB
      rfid.open(5000)

      println("Lecteur Phidget RFID connecté !")
      println("Présente un badge RFID...")

      // Maintenir le programme actif
      while (true) {
        Thread.sleep(1000)
      }

    } catch {
      case e: PhidgetException =>
        println(s"Erreur Phidget : ${e.getMessage}")

      case e: InterruptedException =>
        Thread.currentThread().interrupt()

    } finally {
      rfid.close()
    }
  }
}
