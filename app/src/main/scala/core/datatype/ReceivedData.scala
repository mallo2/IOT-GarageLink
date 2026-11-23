package core.datatype

/**
 * Type to represent the data received form the sensor
 */
sealed trait ReceivedData

case class ReceivedParticuleData(
                                  id: String,
                                  ufp: Double,
                                  fp: Double,
                                  p: Double,
                                  time: String
                                ) extends ReceivedData

case class ReceivedTempData(
                             id: String,
                             temperature: Double,
                             humidity: Double,
                             time: String
                           ) extends ReceivedData


case class ReceivedRfidData(
                              tag: String,
                              time: String
                            ) extends ReceivedData