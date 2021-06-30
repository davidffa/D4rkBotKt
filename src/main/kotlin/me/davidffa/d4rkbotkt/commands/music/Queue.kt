package me.davidffa.d4rkbotkt.commands.music

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.sendPaginator
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.time.Instant

class Queue : Command(
    "queue",
    "Vê as músicas que estão na queue.",
    listOf("q"),
    botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
    category = "Music",
    cooldown = 6
) {
    override suspend fun run(ctx: CommandContext) {
        val musicManager = PlayerManager.musicManagers[ctx.guild.idLong]

        if (musicManager == null) {
            ctx.channel.sendMessage(":x: Não estou a tocar nada de momento!").queue()
            return
        }

        val scheduler = musicManager.scheduler

        if (scheduler.queue.isEmpty()) {
            ctx.channel.sendMessage(":x: A queue está vazia!").queue()
            return
        }

        val header = "<a:disco:803678643661832233> **A tocar:** `${scheduler.current.track.info.title}` " +
                "(Requisitado por `${scheduler.current.requester.user.asTag}`)\n" +
                ":alarm_clock: Tempo total da queue (${Utils.msToHour(scheduler.queue.sumOf { it.track.duration })}) " +
                "----- Total de músicas na queue: ${scheduler.queue.size}\n"

        if (scheduler.queue.size <= 10) {
            val embed = Embed {
                title = ":bookmark_tabs: Lista de músicas"
                description = header +
                        scheduler.queue.mapIndexed { index, track ->
                            "${index+1}º - `${track.track.info.title}` (Requisitado por `${track.requester.user.asTag}`)"
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
                title = ":bookmark_tabs: Lista de músicas"
                description = header +
                        it.mapIndexed { index, track ->
                            "${index + (chunkedQueue.indexOf(it) * 10) + 1}º - `${track.track.info.title}` (Requisitado por `${track.requester.user.asTag}`)"
                        }.joinToString("\n")
                color = Utils.randColor()
                footer {
                    name = "Página ${chunkedQueue.indexOf(it)+1} de ${chunkedQueue.size}"
                    iconUrl = ctx.author.effectiveAvatarUrl
                }
                timestamp = Instant.now()
            }
        }.toTypedArray()

        ctx.channel.sendPaginator(*pages, expireAfter = 10 * 60 * 1000L, filter = {
            if (it.user.idLong == ctx.author.idLong) return@sendPaginator true
            return@sendPaginator false
        }).queue()
    }
}