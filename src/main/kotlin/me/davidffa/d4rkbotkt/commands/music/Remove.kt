package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.audio.Track
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Remove : Command(
  "remove",
  "Remove uma música da queue.",
  listOf("remover"),
  "<Posição>",
  "Music",
  listOf(Permission.MESSAGE_WRITE),
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
        ctx.selfMember,
        ctx.member,
        ctx.channel,
        forOwnTrack = true,
        forAllQueueTracks = false,
        pos
      )
    ) return

    val musicManager = PlayerManager.getMusicManager(ctx.guild.idLong)

    val tracks = mutableListOf<Track>()
    musicManager.scheduler.queue.drainTo(tracks)
    tracks.removeAt(pos - 1)
    musicManager.scheduler.queue.addAll(tracks)

    ctx.channel.sendMessage(ctx.t("commands.remove", listOf(pos.toString()))).queue()
  }
}