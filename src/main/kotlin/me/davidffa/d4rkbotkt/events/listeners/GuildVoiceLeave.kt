package me.davidffa.d4rkbotkt.events.listeners

import dev.minn.jda.ktx.coroutines.await
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Translator
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.audio.receive.ReceiverManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.utils.FileUpload
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

suspend fun onGuildVoiceLeave(event: GuildVoiceUpdateEvent) {
  val receiverManager = ReceiverManager.receiveManagers[event.guild.idLong]

  if (receiverManager != null && event.member.idLong == event.jda.selfUser.idLong) {
    event.guild.audioManager.receivingHandler = null
    event.guild.audioManager.closeAudioConnection()

    receiverManager.timer.cancel()
    receiverManager.audioReceiver.close()

    ReceiverManager.receiveManagers.remove(event.guild.idLong)

    val file = File("./records/record-${event.guild.id}.mp3")

    receiverManager.textChannel
      .sendMessage(Translator.t("commands.record.stop", D4rkBot.guildCache[event.guild.idLong]!!.locale, null))
      .addFiles(FileUpload.fromData(file, "record.mp3"))
      .await()

    file.delete()
    return
  }

  val manager = PlayerManager.musicManagers[event.guild.idLong] ?: return

  val member = event.member

  if (member.idLong == event.jda.selfUser.idLong) {
    if (Utils.hasPermissions(member.guild.selfMember, manager.textChannel, listOf(Permission.MESSAGE_SEND))) {
      manager.textChannel.sendMessage(
        Translator.t(
          "events.voice.disconnected",
          D4rkBot.guildCache[event.guild.idLong]!!.locale,
          null
        )
      )
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

  if (!member.user.isBot && selfChannel != null && event.oldValue!!.idLong == selfChannel.idLong && selfChannel.members.none { !it.user.isBot }) {
    manager.audioPlayer.isPaused = true

    if (Utils.hasPermissions(member.guild.selfMember, manager.textChannel, listOf(Permission.MESSAGE_SEND))) {
      manager.textChannel.sendMessage(
        Translator.t(
          "events.voice.leaveWarning",
          D4rkBot.guildCache[event.guild.idLong]!!.locale,
          null
        )
      )
        .queue {
          manager.leaveMessage = it.idLong
        }
    }

    val timer = Timer()
    manager.leaveTimer = timer

    timer.schedule(timerTask {
      if (Utils.hasPermissions(member.guild.selfMember, manager.textChannel, listOf(Permission.MESSAGE_SEND))) {
        manager.textChannel.sendMessage(
          Translator.t(
            "events.voice.leave",
            D4rkBot.guildCache[event.guild.idLong]!!.locale,
            null
          )
        )
          .queue()

        if (manager.leaveMessage != null) {
          manager.textChannel.deleteMessageById(manager.leaveMessage!!).queue()
        }
        manager.scheduler.destroy()
      }
    }, 2 * 60 * 1000L)
  }
}