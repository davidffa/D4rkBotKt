package com.github.davidffa.d4rkbotkt.commands.music

import com.github.davidffa.d4rkbotkt.audio.PlayerManager
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Skip : Command(
  "skip",
  listOf("s", "pular"),
  category = "Music",
  botPermissions = listOf(Permission.MESSAGE_SEND),
  cooldown = 2
) {
  override suspend fun run(ctx: CommandContext) {
    if (!Utils.canUsePlayer(ctx::t, ctx.selfMember, ctx.member, ctx.channel, true)) return

    val musicManager = PlayerManager.getMusicManager(ctx.guild.idLong)

    if (musicManager.scheduler.queue.isNotEmpty()) {
      ctx.channel.sendMessage(ctx.t("commands.skip")).queue()
    }
    musicManager.scheduler.nextTrack()
  }
}