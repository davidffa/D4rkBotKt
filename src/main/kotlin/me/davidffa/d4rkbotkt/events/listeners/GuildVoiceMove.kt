package me.davidffa.d4rkbotkt.events.listeners

import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Translator
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import java.util.*
import kotlin.concurrent.timerTask

fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
  val manager = PlayerManager.musicManagers[event.guild.idLong] ?: return
  val member = event.member
  val selfChannel = event.guild.audioManager.connectedChannel

  if (selfChannel != null && (member.idLong == event.jda.selfUser.idLong || event.oldValue.idLong == selfChannel.idLong || event.newValue.idLong == selfChannel.idLong)) {
    if (selfChannel.members.none { !it.user.isBot }) {
      if (manager.leaveTimer != null) return
      manager.audioPlayer.isPaused = true

      if (Utils.hasPermissions(member.guild.selfMember, manager.textChannel, listOf(Permission.MESSAGE_WRITE))) {
        manager.textChannel.sendMessage(Translator.t("events.voice.leaveWarning", D4rkBot.guildCache[event.guild.idLong]!!.locale, null))
          .queue {
            manager.leaveMessage = it.idLong
          }
      }

      val timer = Timer()
      manager.leaveTimer = timer

      timer.schedule(timerTask {
        if (Utils.hasPermissions(
            member.guild.selfMember,
            manager.textChannel,
            listOf(Permission.MESSAGE_WRITE)
          )
        ) {
          manager.textChannel.sendMessage(Translator.t("events.voice.leave", D4rkBot.guildCache[event.guild.idLong]!!.locale, null))
            .queue()

          if (manager.leaveMessage != null) {
            manager.textChannel.deleteMessageById(manager.leaveMessage!!).queue()
          }

          manager.scheduler.destroy()
        }
      }, 2 * 60 * 1000L)
      return
    }
    if (manager.leaveTimer != null) {
      manager.audioPlayer.isPaused = false
      manager.leaveTimer?.cancel()
      manager.leaveTimer = null
      manager.textChannel.deleteMessageById(manager.leaveMessage!!).queue()
    }
  }
}