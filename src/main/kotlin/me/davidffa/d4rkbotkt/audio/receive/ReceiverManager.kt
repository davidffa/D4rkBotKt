package me.davidffa.d4rkbotkt.audio.receive

import java.util.*
import kotlin.collections.HashMap

object ReceiverManager {
  val receiveManagers = HashMap<Long, Receiver>()
}

data class Receiver(val audioReceiver: AudioReceiver, val timer: Timer)