package me.davidffa.d4rkbotkt.events.listeners

import me.davidffa.d4rkbotkt.audio.PlayerManager
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent

fun onGuildVoiceJoin(event: GuildVoiceUpdateEvent) {
  val manager = PlayerManager.musicManagers[event.guild.idLong] ?: return
  if (event.member.user.isBot) return
  val selfChannel = event.guild.audioManager.connectedChannel ?: return

  if (event.newValue!!.idLong == selfChannel.idLong && manager.leaveTimer != null) {
    manager.audioPlayer.isPaused = false
    manager.leaveTimer?.cancel()
    manager.leaveTimer = null
    manager.textChannel.deleteMessageById(manager.leaveMessage!!).queue()
  }
}