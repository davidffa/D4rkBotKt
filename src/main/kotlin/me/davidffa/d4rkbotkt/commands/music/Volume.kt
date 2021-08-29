package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Volume : Command(
  "volume",
  listOf("vol"),
  "Music",
  listOf(Permission.MESSAGE_WRITE),
  cooldown = 4
) {
  override suspend fun run(ctx: CommandContext) {
    if (!Utils.canUsePlayer(ctx::t, ctx.selfMember, ctx.member, ctx.channel, true)) return

    val manager = PlayerManager.getMusicManager(ctx.guild.idLong)

    if (ctx.args.isEmpty()) {
      ctx.channel.sendMessage(ctx.t("commands.volume.current", listOf((manager.volume * 100f).toInt().toString()))).queue()
      return
    }

    val vol = ctx.args[0].toIntOrNull()

    if (vol == null || vol < 0 || vol > 500) {
      ctx.channel.sendMessage(ctx.t("commands.volume.range")).queue()
      return
    }

    manager.volume = vol / 100f
    ctx.channel.sendMessage(ctx.t("commands.volume.changed", listOf((manager.volume * 100f).toInt().toString()))).queue()
  }
}