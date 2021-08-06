package me.davidffa.d4rkbotkt.commands.others

import dev.minn.jda.ktx.await
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.audio.receive.AudioReceiver
import me.davidffa.d4rkbotkt.audio.receive.Receiver
import me.davidffa.d4rkbotkt.audio.receive.ReceiverManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

class Record : Command(
  "record",
  "Grava áudio no canal de voz e envia em MP3.",
  listOf("rec", "gravar"),
  category = "Others",
  cooldown = 8,
  botPermissions = listOf(Permission.MESSAGE_WRITE)
) {
  override suspend fun run(ctx: CommandContext) {
    if (!Utils.canRecord(ctx.selfMember, ctx.member, ctx.channel)) return

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
        .sendMessage(":stop_button: Parei de gravar!")
        .addFile(file, "record.mp3")
        .await()

      file.delete()
      return
    }

    if (!selfVoiceState!!.inVoiceChannel()) {
      ctx.guild.audioManager.isSelfDeafened = false
      ctx.guild.audioManager.isSelfMuted = true
      ctx.guild.audioManager.openAudioConnection(ctx.member.voiceState?.channel)
    }

    val audioReceiver = AudioReceiver(ctx.guild.id)
    ctx.guild.audioManager.receivingHandler = audioReceiver
    ctx.channel.sendMessage(":red_circle: Comecei a gravar (máximo de 8 minutos)!").queue()

    val timer = Timer()
    timer.schedule(timerTask {
      ctx.guild.audioManager.receivingHandler = null
      ctx.guild.audioManager.closeAudioConnection()

      ReceiverManager.receiveManagers[ctx.guild.idLong]!!.audioReceiver.close()
      ReceiverManager.receiveManagers.remove(ctx.guild.idLong)

      val file = File("./records/record-${ctx.guild.id}.mp3")

      ctx.channel
        .sendMessage(":stop_button: Parei de gravar! (foi atingido o limite de 8 minutos).")
        .addFile(file)
        .queue {
          file.delete()
        }
    }, 8 * 60 * 1000L)

    ReceiverManager.receiveManagers[ctx.guild.idLong] = Receiver(audioReceiver, timer)
  }
}