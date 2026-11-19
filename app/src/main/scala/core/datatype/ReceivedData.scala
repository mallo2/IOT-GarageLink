package core.datatype


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
                              tag: String
                            ) extends ReceivedData