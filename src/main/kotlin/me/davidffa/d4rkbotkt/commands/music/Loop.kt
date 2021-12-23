package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Loop : Command(
  "loop",
  listOf("repeat"),
  "Music",
  botPermissions = listOf(Permission.MESSAGE_SEND),
  cooldown = 4,
  args = 1
) {
  override suspend fun run(ctx: CommandContext) {
    if (!Utils.canUsePlayer(ctx::t, ctx.selfMember, ctx.member, ctx.channel)) return

    val musicManager = PlayerManager.getMusicManager(ctx.guild.idLong)

    when (ctx.args[0].lowercase()) {
      "track" -> {
        musicManager.scheduler.trackLoop = !musicManager.scheduler.trackLoop
        if (musicManager.scheduler.trackLoop) ctx.channel.sendMessage(ctx.t("commands.loop.track.switchOn"))
          .queue()
        else ctx.channel.sendMessage(ctx.t("commands.loop.track.switchOff")).queue()
      }
      "queue" -> {
        musicManager.scheduler.queueLoop = !musicManager.scheduler.queueLoop
        if (musicManager.scheduler.queueLoop) ctx.channel.sendMessage(ctx.t("commands.loop.queue.switchOn")).queue()
        else ctx.channel.sendMessage(ctx.t("commands.loop.queue.switchOff")).queue()
      }
      else -> {
        ctx.channel.sendMessage(
          ctx.t(
            "commands.loop.wrongusage",
            listOf("${ctx.prefix}${this.name} ${ctx.t("usages.${this.name}")}")
          )
        ).queue()
      }
    }
  }
}