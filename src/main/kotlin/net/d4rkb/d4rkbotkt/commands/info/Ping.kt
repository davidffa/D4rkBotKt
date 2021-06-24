package net.d4rkb.d4rkbotkt.commands.info

import com.mongodb.client.model.Filters
import net.d4rkb.d4rkbotkt.Database
import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import java.awt.Color
import java.time.Instant

class Ping : Command(
    "ping",
    "Mostra o ping de envio de mensagens e o da API",
    aliases = listOf("latency", "latencia"),
    category = "Info",
    botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {
    override fun run(ctx: CommandContext) {
        val jda = ctx.jda
        val rest = jda.restPing.complete()
        val gateway = jda.gatewayPing

        val dbTime = Instant.now().toEpochMilli()
        Database.botDB.find(Filters.eq("_id", ctx.selfUser.id)).first()
        val dbPing = Instant.now().toEpochMilli() - dbTime

        val avg = (rest + gateway + dbPing) / 3

        val color = when{
            avg < 150 -> Color.GREEN
            avg < 300 -> Color.ORANGE
            else -> Color.RED
        }

        val embed = EmbedBuilder()
            .setTitle(":ping_pong: Ping")
            .setDescription(":incoming_envelope: `${rest}ms`\n:heartbeat: `${gateway}ms`\n<:MongoDB:773610222602158090> `${dbPing}ms`")
            .setColor(color)
            .setFooter(ctx.author.asTag, ctx.author.effectiveAvatarUrl)
            .setTimestamp(Instant.now())
            .build()

        ctx.channel.sendMessageEmbeds(embed).queue()
    }
}