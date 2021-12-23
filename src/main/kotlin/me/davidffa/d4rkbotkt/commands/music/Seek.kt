package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import kotlin.math.pow

class Seek : Command(
  "seek",
  category = "Music",
  botPermissions = listOf(Permission.MESSAGE_SEND),
  args = 1,
  cooldown = 2
) {
  override suspend fun run(ctx: CommandContext) {
    if (!Utils.canUsePlayer(ctx::t, ctx.selfMember, ctx.member, ctx.channel, true)) return

    val track = PlayerManager.getMusicManager(ctx.guild.idLong).scheduler.current.track!!

    var time = 0L

    val chunks = ctx.args[0].split(":")

    for (i in chunks.indices) {
      val num = chunks[chunks.size - i - 1].toLongOrNull()

      if (num == null) {
        ctx.channel.sendMessage(ctx.t("commands.seek.invalidFormat")).queue()
        return
      }

      time += num * 60.0.pow(i).toLong()
    }

    time *= 1000

    if (time > track.duration) {
      ctx.channel.sendMessage(ctx.t("commands.seek.range", listOf(Utils.msToHour(track.duration)))).queue()
      return
    }

    track.position = time

    ctx.channel.sendMessage(ctx.t("commands.seek.seeked", listOf(Utils.msToHour(time)))).queue()
  }
}