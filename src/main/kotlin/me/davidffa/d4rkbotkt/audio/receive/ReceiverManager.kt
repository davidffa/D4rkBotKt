package me.davidffa.d4rkbotkt.audio.receive

import net.dv8tion.jda.api.entities.TextChannel
import java.util.*

object ReceiverManager {
  val receiveManagers = HashMap<Long, Receiver>()
}

data class Receiver(val audioReceiver: AudioReceiver, val timer: Timer, val textChannel: TextChannel)