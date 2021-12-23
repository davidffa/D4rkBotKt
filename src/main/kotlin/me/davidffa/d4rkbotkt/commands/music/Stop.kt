package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Stop : Command(
  "stop",
  listOf("parar", "disconnect", "desconectar", "quit", "leave", "sair"),
  category = "Music",
  botPermissions = listOf(Permission.MESSAGE_SEND),
  cooldown = 4
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

    musicManager.scheduler.destroy()
    ctx.channel.sendMessage(ctx.t("commands.stop")).queue()
  }
}