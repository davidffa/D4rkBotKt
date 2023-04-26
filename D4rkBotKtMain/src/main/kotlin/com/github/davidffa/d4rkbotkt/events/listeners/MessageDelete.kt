package com.github.davidffa.d4rkbotkt.events.listeners

import com.github.davidffa.d4rkbotkt.audio.PlayerManager
import net.dv8tion.jda.api.events.message.MessageDeleteEvent

fun onMessageDelete(event: MessageDeleteEvent) {
  val manager = PlayerManager.musicManagers[event.guild.idLong] ?: return

  when (event.messageIdLong) {
    manager.djtableMessage -> manager.djtableMessage = null
    manager.scheduler.npMessage -> manager.scheduler.npMessage = null
    manager.leaveMessage -> manager.leaveMessage = null
  }
}