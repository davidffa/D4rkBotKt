package me.davidffa.d4rkbotkt.commands.music

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.sendPaginator
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

class Queue : Command(
  "queue",
  listOf("q"),
  botPermissions = listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS),
  category = "Music",
  cooldown = 6
) {
  override suspend fun run(ctx: CommandContext) {
    val musicManager = PlayerManager.musicManagers[ctx.guild.idLong]

    if (musicManager == null) {
      ctx.channel.sendMessage(ctx.t("errors.notplaying")).queue()
      return
    }

    val scheduler = musicManager.scheduler

    if (scheduler.queue.isEmpty()) {
      ctx.channel.sendMessage(ctx.t("errors.emptyqueue")).queue()
      return
    }

    val header = ctx.t(
      "commands.queue.header",
      listOf(
        scheduler.current.title,
        scheduler.current.requester.user.asTag,
        Utils.msToHour(scheduler.queue.sumOf { it.duration }),
        scheduler.queue.size.toString()
      )
    )

    if (scheduler.queue.size <= 10) {
      val embed = Embed {
        title = ctx.t("commands.queue.title")
        description = header +
                scheduler.queue.mapIndexed { index, track ->
                  "${index + 1}º - [${track.title}](${track.uri}) (${
                    ctx.t(
                      "commands.queue.requestedBy",
                      listOf(track.requester.user.asTag)
                    )
                  })"
                }.joinToString("\n")
        color = Utils.randColor()
        footer {
          name = ctx.author.asTag
          iconUrl = ctx.author.effectiveAvatarUrl
        }
        timestamp = Instant.now()
      }

      ctx.channel.sendMessageEmbeds(embed).queue()
      return
    }

    val chunkedQueue = scheduler.queue.chunked(10)

    val pages = chunkedQueue.map {
      Embed {
        title = ctx.t("commands.queue.title")
        description = header +
                it.mapIndexed { index, track ->
                  "${index + (chunkedQueue.indexOf(it) * 10) + 1}º - [${track.title}](${track.uri}) (${
                    ctx.t(
                      "commands.queue.requestedBy",
                      listOf(track.requester.user.asTag)
                    )
                  })"
                }.joinToString("\n")
        color = Utils.randColor()
        footer {
          name = ctx.t(
            "commands.queue.page",
            listOf((chunkedQueue.indexOf(it) + 1).toString(), chunkedQueue.size.toString())
          )
          iconUrl = ctx.author.effectiveAvatarUrl
        }
        timestamp = Instant.now()
      }
    }.toTypedArray()

    ctx.channel.sendPaginator(*pages, expireAfter = 10.minutes, filter = {
      if (it.user.idLong == ctx.author.idLong) return@sendPaginator true
      return@sendPaginator false
    }).queue()
  }
}