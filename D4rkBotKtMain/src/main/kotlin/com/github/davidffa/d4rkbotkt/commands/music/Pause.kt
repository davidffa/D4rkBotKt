package com.github.davidffa.d4rkbotkt.commands.music

import com.github.davidffa.d4rkbotkt.audio.PlayerManager
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Pause : Command(
  "pause",
  listOf("pausa"),
  category = "Music",
  botPermissions = listOf(Permission.MESSAGE_SEND),
  cooldown = 4
) {
  override suspend fun run(ctx: CommandContext) {
    if (!Utils.canUsePlayer(ctx::t, ctx.selfMember, ctx.member, ctx.channel)) return

    val musicManager = PlayerManager.getMusicManager(ctx.guild.idLong)

    if (musicManager.audioPlayer.isPaused) {
      ctx.channel.sendMessage(ctx.t("commands.resume")).queue()
      musicManager.audioPlayer.isPaused = false
    } else {
      ctx.channel.sendMessage(ctx.t("commands.pause")).queue()
      musicManager.audioPlayer.isPaused = true
    }
  }
}