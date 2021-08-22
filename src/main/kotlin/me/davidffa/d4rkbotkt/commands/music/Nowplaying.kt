package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import net.dv8tion.jda.api.Permission

class Nowplaying : Command(
  "nowplaying",
  "Mostra a música que está a tocar.",
  listOf("np", "tocando"),
  botPermissions = listOf(Permission.MESSAGE_WRITE),
  category = "Music",
  cooldown = 5,

  ) {
  override suspend fun run(ctx: CommandContext) {
    val musicManager = PlayerManager.musicManagers[ctx.guild.idLong]

    if (musicManager == null) {
      ctx.channel.sendMessage(ctx.t("errors.notplaying")).queue()
      return
    }

    ctx.channel.sendMessage(ctx.t("commands.nowplaying", listOf(musicManager.scheduler.current.title)))
      .queue()
  }
}