package me.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import net.dv8tion.jda.api.Permission
import java.awt.Color
import java.time.Instant

class Ping : Command(
  "ping",
  listOf("latency", "latencia"),
  category = "Info",
  botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {
  override suspend fun run(ctx: CommandContext) {
    val jda = ctx.jda
    val rest = jda.restPing.await()
    val gateway = jda.gatewayPing

    val dbStart = Instant.now().toEpochMilli()
    Database.botDB.findOneById(ctx.selfUser.id)
    val dbPing = Instant.now().toEpochMilli() - dbStart

    val avg = (rest + gateway + dbPing) / 3

    val color = when {
      avg < 150 -> Color.GREEN
      avg < 300 -> Color.ORANGE
      else -> Color.RED
    }.rgb

    val embed = Embed {
      title = ":ping_pong: Ping"
      description = ":incoming_envelope: `${rest}ms`\n:heartbeat: `${gateway}ms`\n" +
              "<:MongoDB:773610222602158090> `${dbPing}ms`"
      this.color = color
      footer {
        name = ctx.author.asTag
        iconUrl = ctx.author.effectiveAvatarUrl
      }
      timestamp = Instant.now()
    }

    ctx.channel.sendMessageEmbeds(embed).queue()
  }
}