package net.d4rkb.d4rkbotkt.commands.info

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary
import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.Permission
import java.lang.management.ManagementFactory
import java.time.Instant

class Botinfo : Command(
    "botinfo",
    "Informações sobre mim.",
    aliases = listOf("info", "bi"),
    category = "Info",
    botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_EXT_EMOJI),
    cooldown = 10
) {
    override fun run(ctx: CommandContext) {
        val runtime = Runtime.getRuntime()
        val rest = ctx.jda.restPing.complete()

        val embed = EmbedBuilder()
            .setTitle("<a:blobdance:804026401849475094> Informações sobre mim")
            .setColor(Utils.randColor())
            .setDescription("**[Convite](https://discord.com/oauth2/authorize?client_id=${ctx.jda.selfUser.id}&scope=bot&permissions=8)**\n" +
                    "**[Servidor de Suporte](https://discord.gg/dBQnxVCTEw)**\n\n")
            .addField(":id: Meu ID", "`${ctx.selfUser.id}`", true)
            .addField(":man: Dono", "`D4rkB#2408`", true)
            .addField(":calendar: Criado em", "`${Utils.formatDate(ctx.selfUser.timeCreated)}`", true)
            .addField("<a:infinity:838759634361253929> Uptime",
                "`${Utils.msToDate(ManagementFactory.getRuntimeMXBean().uptime)}`",
                true)
            .addField(":desktop: Servidores", "`${ctx.jda.guilds.size}`", true)
            .addField(":ping_pong: Ping", "REST: `${rest}ms`\nGateway: `${ctx.jda.gatewayPing}ms`", true)
            .addField("<:badgehypesquad:803665497223987210> Prefixos", "Padrão: `dk.`", true)
            .addField("<:ram:751468688686841986> RAM",
                "Usada: `${(runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024}MB`\n" +
                        "Alocada: `${runtime.totalMemory() / 1024 / 1024}MB`",
                true)
            .addField("<:kotlin:856168010004037702> Versões:", "Kotlin: `${KotlinVersion.CURRENT}`\n" +
                    "JVM: `${System.getProperty("java.version")}`\n" +
                    "JDA: `${JDAInfo.VERSION}`\n" +
                    "Lavaplayer: `${PlayerLibrary.VERSION}`", true)
            .setThumbnail(ctx.selfUser.effectiveAvatarUrl)
            .setFooter(ctx.author.asTag, ctx.author.effectiveAvatarUrl)
            .setTimestamp(Instant.now())
            .build()

        ctx.channel.sendMessageEmbeds(embed).queue()
    }
}