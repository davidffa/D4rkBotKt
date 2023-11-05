package com.github.davidffa.d4rkbotkt.commands.music

import dev.minn.jda.ktx.messages.Embed
import com.github.davidffa.d4rkbotkt.audio.PlayerManager
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.time.Instant

class Nowplaying : Command(
  "nowplaying",
  listOf("np", "tocando"),
  botPermissions = listOf(Permission.MESSAGE_SEND),
  category = "Music",
  cooldown = 5,

  ) {
  override suspend fun run(ctx: CommandContext) {
    val musicManager = PlayerManager.musicManagers[ctx.guild.idLong]

    if (musicManager == null) {
      ctx.channel.sendMessage(ctx.t("errors.notplaying")).queue()
      return
    }

    val track = musicManager.scheduler.current.track!!

    val embed = Embed {
      title = ctx.t("commands.nowplaying.title")
      color = Utils.randColor()
      if (!track.info.isStream) {
        description = "```py\n" +
                getProgressBar((track.position / 1000).toInt(), (track.info.length / 1000).toInt()) +
                "\n${Utils.msToHour(track.position)}                     " +
                Utils.msToHour(track.info.length) +
                "\n```"
      }
      field {
        name = ctx.t("commands.nowplaying.name")
        value = "`${musicManager.scheduler.current.title}`"
        inline = false
      }
      field {
        name = ctx.t("commands.nowplaying.author")
        value = "`${track.info.author}`"
        inline = false
      }
      field {
        name = ctx.t("commands.nowplaying.requester")
        value = "`${musicManager.scheduler.current.requester.user.name}`"
        inline = false
      }
      url = track.info.uri
      thumbnail = track.info.artworkUrl
      timestamp = Instant.now()
      footer {
        name = ctx.author.name
        iconUrl = ctx.author.effectiveAvatarUrl
      }
    }

    ctx.channel.sendMessageEmbeds(embed).queue()
  }

  private fun getProgressBar(current: Int, total: Int): String {
    val progress = 29 * current / total

    return "${"━".repeat(if (progress > 0) progress - 1 else 0)}\uD83D\uDD18${"━".repeat(29 - progress)}"
  }
}