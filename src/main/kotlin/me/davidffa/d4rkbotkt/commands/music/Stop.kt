package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Stop : Command(
  "stop",
  "Para de tocar música e limpa a lista de músicas.",
  listOf("parar", "disconnect", "desconectar", "quit", "leave", "sair"),
  category = "Music",
  botPermissions = listOf(Permission.MESSAGE_WRITE),
  cooldown = 4
) {
  override suspend fun run(ctx: CommandContext) {
    if (!Utils.canUsePlayer(ctx.selfMember, ctx.member, ctx.channel)) return

    val musicManager = PlayerManager.getMusicManager(ctx.guild.idLong)

    musicManager.scheduler.destroy()
    ctx.channel.sendMessage(":stop_button: Parei a música!").queue()
  }
}