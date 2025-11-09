package com.github.davidffa.d4rkbotkt.commands.others

import dev.minn.jda.ktx.coroutines.await
import com.github.davidffa.d4rkbotkt.audio.receive.AudioReceiver
import com.github.davidffa.d4rkbotkt.audio.receive.Receiver
import com.github.davidffa.d4rkbotkt.audio.receive.ReceiverManager
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.utils.FileUpload
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

class Record : Command(
  "record",
  listOf("rec", "gravar"),
  category = "Others",
  cooldown = 8,
  botPermissions = listOf(Permission.MESSAGE_SEND)
) {
  override suspend fun run(ctx: CommandContext) {
    if (!Utils.canRecord(ctx::t, ctx.selfMember, ctx.member, ctx.channel)) return

    val selfVoiceState = ctx.selfMember.voiceState

    val receiverManager = ReceiverManager.receiveManagers[ctx.guild.idLong]

    if (receiverManager != null) {
      ctx.guild.audioManager.receivingHandler = null
      ctx.guild.audioManager.closeAudioConnection()

      receiverManager.timer.cancel()
      receiverManager.audioReceiver.close()

      ReceiverManager.receiveManagers.remove(ctx.guild.idLong)

      val file = File("./records/record-${ctx.guild.id}.mp3")

      ctx.channel
        .sendMessage(ctx.t("commands.record.stop"))
        .addFiles(FileUpload.fromData(file, "record.mp3"))
        .await()

      file.delete()
      return
    }

    if (!selfVoiceState!!.inAudioChannel()) {
      ctx.guild.audioManager.isSelfDeafened = false
      ctx.guild.audioManager.isSelfMuted = true
      ctx.guild.audioManager.openAudioConnection(ctx.member.voiceState?.channel as AudioChannel)
    }

    val audioReceiver = AudioReceiver(ctx.guild.id, ctx.member.voiceState!!.channel!!.bitrate)
    ctx.guild.audioManager.receivingHandler = audioReceiver
    ctx.channel.sendMessage(ctx.t("commands.record.start")).queue()

    val timer = Timer()
    timer.schedule(timerTask {
      ctx.guild.audioManager.receivingHandler = null
      ctx.guild.audioManager.closeAudioConnection()

      ReceiverManager.receiveManagers[ctx.guild.idLong]!!.audioReceiver.close()
      ReceiverManager.receiveManagers.remove(ctx.guild.idLong)

      val file = File("./records/record-${ctx.guild.id}.mp3")

      ctx.channel
        .sendMessage(ctx.t("commands.record.timeout"))
        .addFiles(FileUpload.fromData(file, "record.mp3"))
        .queue {
          file.delete()
        }
    }, 8 * 60 * 1000L)

    ReceiverManager.receiveManagers[ctx.guild.idLong] = Receiver(audioReceiver, timer, ctx.channel)
  }
}