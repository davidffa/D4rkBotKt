package me.davidffa.d4rkbotkt.events.listeners

import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Translator
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import java.util.*
import kotlin.concurrent.timerTask

fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
  val manager = PlayerManager.musicManagers[event.guild.idLong] ?: return

  val member = event.member

  if (member.idLong == event.jda.selfUser.idLong) {
    if (Utils.hasPermissions(member.guild.selfMember, manager.textChannel, listOf(Permission.MESSAGE_WRITE))) {
      manager.textChannel.sendMessage(Translator.t("events.voice.disconnected", D4rkBot.guildCache[event.guild.idLong]!!.locale, null))
        .queue()
    }

    if (manager.leaveMessage != null) {
      manager.leaveTimer?.cancel()
      manager.textChannel.deleteMessageById(manager.leaveMessage!!).queue()
    }
    manager.scheduler.destroy()
    return
  }

  val selfChannel = event.guild.audioManager.connectedChannel

  if (!member.user.isBot && selfChannel != null && event.oldValue.idLong == selfChannel.idLong && selfChannel.members.none { !it.user.isBot }) {
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
      if (Utils.hasPermissions(member.guild.selfMember, manager.textChannel, listOf(Permission.MESSAGE_WRITE))) {
        manager.textChannel.sendMessage(Translator.t("events.voice.leave", D4rkBot.guildCache[event.guild.idLong]!!.locale, null))
          .queue()

        if (manager.leaveMessage != null) {
          manager.textChannel.deleteMessageById(manager.leaveMessage!!).queue()
        }
        manager.scheduler.destroy()
      }
    }, 2 * 60 * 1000L)
  }
}