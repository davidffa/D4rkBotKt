package me.davidffa.d4rkbotkt.events.listeners

import me.davidffa.d4rkbotkt.audio.PlayerManager
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent

fun onGuildMessageDelete(event: GuildMessageDeleteEvent) {
  val manager = PlayerManager.musicManagers[event.guild.idLong] ?: return

  when (event.messageIdLong) {
    manager.djtableMessage -> manager.djtableMessage = null
    manager.scheduler.npMessage -> manager.scheduler.npMessage = null
    manager.leaveMessage -> manager.leaveMessage = null
  }
}