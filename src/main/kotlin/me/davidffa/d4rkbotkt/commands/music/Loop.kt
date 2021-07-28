package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Loop : Command(
  "loop",
  "Repete a queue ou a música atual.",
  listOf("repeat"),
  "<Track/Queue>",
  "Music",
  botPermissions = listOf(Permission.MESSAGE_WRITE),
  cooldown = 4,
  args = 1
) {
  override suspend fun run(ctx: CommandContext) {
    if (!Utils.canUsePlayer(ctx.selfMember, ctx.member, ctx.channel)) return

    val musicManager = PlayerManager.getMusicManager(ctx.guild.idLong)

    when (ctx.args[0].lowercase()) {
      "track" -> {
        musicManager.scheduler.trackLoop = !musicManager.scheduler.trackLoop
        if (musicManager.scheduler.trackLoop) ctx.channel.sendMessage("<a:disco:803678643661832233> Música atual em loop!")
          .queue()
        else ctx.channel.sendMessage("<a:disco:803678643661832233> Loop da música atual desativado!").queue()
      }
      "queue" -> {
        musicManager.scheduler.queueLoop = !musicManager.scheduler.queueLoop
        if (musicManager.scheduler.queueLoop) ctx.channel.sendMessage(":bookmark_tabs: Queue em loop!").queue()
        else ctx.channel.sendMessage(":bookmark_tabs: Loop da queue desativado!").queue()
      }
      else -> {
        val prefix = "dk."

        ctx.channel.sendMessage(":x: **Usa:** `$prefix${this.name} ${this.usage}`").queue()
      }
    }
  }
}