package me.davidffa.d4rkbotkt.audio.receive

import java.util.*

object ReceiverManager {
  val receiveManagers = HashMap<Long, Receiver>()
}

data class Receiver(val audioReceiver: AudioReceiver, val timer: Timer)