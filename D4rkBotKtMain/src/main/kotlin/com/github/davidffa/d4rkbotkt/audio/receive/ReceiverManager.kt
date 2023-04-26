package com.github.davidffa.d4rkbotkt.audio.receive

import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import java.util.*

object ReceiverManager {
  val receiveManagers = HashMap<Long, Receiver>()
}

data class Receiver(val audioReceiver: AudioReceiver, val timer: Timer, val textChannel: GuildMessageChannel)