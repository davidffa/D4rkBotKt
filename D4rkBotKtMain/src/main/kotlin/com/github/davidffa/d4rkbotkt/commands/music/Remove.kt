package com.github.davidffa.d4rkbotkt.commands.music

import com.github.davidffa.d4rkbotkt.audio.PlayerManager
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Remove : Command(
  "remove",
  listOf("remover"),
  "Music",
  listOf(Permission.MESSAGE_SEND),
  args = 1,
  cooldown = 2
) {
  override suspend fun run(ctx: CommandContext) {
    val pos = ctx.args[0].toIntOrNull()
    if (pos == null) {
      ctx.channel.sendMessage(ctx.t("errors.invalidNumber")).queue()
      return
    }

    if (!Utils.canUsePlayer(
        ctx::t,
        ctx.selfMember,
        ctx.member,
        ctx.channel,
        forOwnTrack = true,
        forAllQueueTracks = false,
        pos
      )
    ) return

    val musicManager = PlayerManager.getMusicManager(ctx.guild.idLong)

    musicManager.scheduler.queue.removeAt(pos - 1)

    ctx.channel.sendMessage(ctx.t("commands.remove", listOf(pos.toString()))).queue()
  }
}