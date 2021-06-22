package net.d4rkb.d4rkbotkt.commands.info

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

        jda.restPing.queue{ ping ->
            val gatewayPing = jda.gatewayPing

            val avg = (ping + gatewayPing) / 2

            val color = when{
                avg < 150 -> Color.GREEN
                avg < 300 -> Color.ORANGE
                else -> Color.RED
            }

            val embed = EmbedBuilder()
                .setTitle(":ping_pong: Ping")
                .setDescription(":incoming_envelope: `${ping}ms`\n:heartbeat: `${gatewayPing}ms`")
                .setColor(color)
                .setFooter(ctx.author.asTag, ctx.author.effectiveAvatarUrl)
                .setTimestamp(Instant.now())
                .build()

            ctx.channel.sendMessageEmbeds(embed).queue()
        }
    }
}