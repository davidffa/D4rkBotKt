package com.github.davidffa.d4rkbotkt.commands.music

import com.github.davidffa.d4rkbotkt.audio.PlayerManager
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Shuffle : Command(
  "shuffle",
  category = "Music",
  cooldown = 8,
  botPermissions = listOf(Permission.MESSAGE_SEND),
) {
  override suspend fun run(ctx: CommandContext) {
    if (!Utils.canUsePlayer(
        ctx::t,
        ctx.selfMember,
        ctx.member,
        ctx.channel,
        forOwnTrack = false,
        forAllQueueTracks = true
      )
    ) return

    val musicManager = PlayerManager.getMusicManager(ctx.guild.idLong)

    if (musicManager.scheduler.queue.isEmpty()) {
      ctx.channel.sendMessage(ctx.t("errors.emptyqueue")).queue()
      return
    }

    musicManager.scheduler.queue.shuffle()
    ctx.channel.sendMessage(ctx.t("commands.shuffle")).queue()
  }
}