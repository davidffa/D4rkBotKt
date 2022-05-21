package me.davidffa.d4rkbotkt.events.listeners

import dev.minn.jda.ktx.coroutines.await
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Translator
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.audio.receive.ReceiverManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

suspend fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
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
      .addFile(file, "record.mp3")
      .await()

    file.delete()
    return
  }

  val manager = PlayerManager.musicManagers[event.guild.idLong] ?: return
  val member = event.member
  val selfChannel = event.guild.audioManager.connectedChannel

  if (selfChannel != null && (member.idLong == event.jda.selfUser.idLong || event.oldValue.idLong == selfChannel.idLong || event.newValue.idLong == selfChannel.idLong)) {
    if (selfChannel.members.none { !it.user.isBot }) {
      if (manager.leaveTimer != null) return
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
        if (Utils.hasPermissions(
            member.guild.selfMember,
            manager.textChannel,
            listOf(Permission.MESSAGE_SEND)
          )
        ) {
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