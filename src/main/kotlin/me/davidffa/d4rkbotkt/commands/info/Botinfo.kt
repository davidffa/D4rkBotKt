package me.davidffa.d4rkbotkt.commands.info

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.Permission
import oshi.SystemInfo
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
    private val si = SystemInfo()
    private var cpuTime = 0.0
    private var uptime = 0.0
    private var lastSystemCpuLoadTicks: LongArray? = null

    override suspend fun run(ctx: CommandContext) {
        val runtime = Runtime.getRuntime()
        val rest = ctx.jda.restPing.await()

        val dbStart = Instant.now().toEpochMilli()
        Database.botDB.findOneById(ctx.selfUser.id)
        val dbPing = Instant.now().toEpochMilli() - dbStart

        var processLoad = getProcessRecentCpuUsage()
        if (processLoad.isInfinite()) processLoad = 0.0

        val systemLoad = getSystemRecentCpuUsage()

        val embed = Embed {
            title = "<a:blobdance:804026401849475094> Informações sobre mim"
            description = "**[Convite](https://discord.com/oauth2/authorize?client_id=${ctx.jda.selfUser.id}&scope=bot&permissions=8)**\n" +
                    "**[Servidor de Suporte](https://discord.gg/dBQnxVCTEw)**\n\n"
            color = 15695386
            field {
                name = ":id: Meu ID"
                value = "`${ctx.selfUser.id}`"
            }
            field {
                name = ":calendar: Criado em"
                value = "`${Utils.formatDate(ctx.selfUser.timeCreated)}`"
            }
            field {
                name = "<a:infinity:838759634361253929> Uptime"
                value = "`${Utils.msToDate(ManagementFactory.getRuntimeMXBean().uptime)}`"
            }
            field {
                name = ":desktop: Servidores"
                value = "`${ctx.jda.guilds.size}`"
            }
            field {
                name = "<:badgehypesquad:803665497223987210> Prefixos"
                value = "Padrão: `dk.`\nNo servidor: `${ctx.prefix}`"
            }
            field {
                name = ":ping_pong: Pings"
                value = "REST: `${rest}ms`\nGateway: `${ctx.jda.gatewayPing}ms`\nMongoDB: `${dbPing}ms`"
            }
            field {
                name = "<:ram:751468688686841986> RAM"
                value = "Usada: `${(runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024}MB`\n" +
                        "Alocada: `${runtime.totalMemory() / 1024 / 1024}MB`"
            }
            field {
                name = "<a:loading:804026048647659540> CPU"
                value = "Sistema: `${"%.2f".format(systemLoad * 100)}%`\n" +
                        "Bot: `${"%.2f".format(processLoad * 100)}%`"
            }
            field {
                name = "<:kotlin:856168010004037702> Versões:"
                value = "Kotlin: `${KotlinVersion.CURRENT}`\n" +
                        "JVM: `${System.getProperty("java.version")}`\n" +
                        "JDA: `${JDAInfo.VERSION}`\n" +
                        "Lavaplayer: `${PlayerLibrary.VERSION}`"
            }
            thumbnail = ctx.selfUser.effectiveAvatarUrl
            footer {
                name = ctx.author.asTag
                iconUrl = ctx.author.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }

        ctx.channel.sendMessageEmbeds(embed).queue()
    }

    private fun getSystemRecentCpuUsage(): Double {
        val hal = si.hardware
        val processor = hal.processor

        if (lastSystemCpuLoadTicks == null) {
            lastSystemCpuLoadTicks = processor.systemCpuLoadTicks
        }

        return processor.getSystemCpuLoadBetweenTicks(lastSystemCpuLoadTicks)
    }

    private fun getProcessRecentCpuUsage(): Double {
        val output: Double
        val hal = si.hardware
        val os = si.operatingSystem
        val p = os.getProcess(os.processId)

        output = if (cpuTime != 0.0) {
            val uptimeDiff = p.upTime - uptime
            val cpuDiff = (p.kernelTime + p.userTime) - cpuTime
            cpuDiff / uptimeDiff
        }else {
            ((p.kernelTime + p.userTime).toDouble() / p.userTime.toDouble())
        }

        uptime = p.upTime.toDouble()
        cpuTime = (p.kernelTime + p.userTime).toDouble()
        return output / hal.processor.logicalProcessorCount
    }
}