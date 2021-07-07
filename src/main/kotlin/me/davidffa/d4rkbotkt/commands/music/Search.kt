package me.davidffa.d4rkbotkt.commands.music

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import dev.minn.jda.ktx.*
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.time.Instant
import java.util.*
import kotlin.concurrent.timerTask

class Search : Command(
    "search",
    "Procura uma música no YouTube ou na SoundCloud e toca-a.",
    listOf("procurar", "searchmusic"),
    "[yt/sc] <Nome da música>",
    "Music",
    listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
    args = 1,
    cooldown = 5
) {
    private val pendingSearches = mutableListOf<Long>()

    override suspend fun run(ctx: CommandContext) {
        if (pendingSearches.contains(ctx.author.idLong)) return
        if (!Utils.canPlay(ctx.selfMember, ctx.member, ctx.channel)) return

        val query = if (ctx.args[0].lowercase() == "sc" || ctx.args[0].lowercase() == "yt") {
            if (ctx.args.size < 2) {
                ctx.channel.sendMessage(":x: **Usa:** ${ctx.prefix}search [yt/sc] <Nome da música>").queue()
                return
            }
            if (Utils.isUrl(ctx.args[1])) ctx.args[1]
            else "${ctx.args[0]}search:${ctx.args.subList(1, ctx.args.size).joinToString(" ")}"
        }else {
            if (Utils.isUrl(ctx.args[0])) ctx.args[0]
            else "ytsearch:${ctx.args.joinToString(" ")}"
        }

        val tracks = try {
            PlayerManager.search(query, 10)
        }catch (e: IllegalStateException) {
            ctx.channel.sendMessage(":x: Não encontrei nenhum resultado!").queue()
            null
        }catch (e: FriendlyException) {
            ctx.channel.sendMessage(":x: Ocorreu um erro ao procurar a música!\nErro: ${e.message}").queue()
            null
        } ?: return

        pendingSearches.add(ctx.author.idLong)
        val embed = Embed {
            title = ":mag: Resultados da procura"
            description = "Envie mensagem com o número da música, (0 para cancelar)\n\n" +
                    tracks.mapIndexed { i, track ->
                        "${i+1}º - `${track.info.title}`"
                    }.joinToString("\n")
            color = Utils.randColor()
            footer {
                name = ctx.author.asTag
                iconUrl = ctx.author.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }

        val msg = ctx.channel.sendMessageEmbeds(embed).await()

        var listener: CoroutineEventListener? = null

        val timer = Timer()
        timer.schedule(timerTask {
            pendingSearches.remove(ctx.author.idLong)
            ctx.jda.removeEventListener(listener)
            msg.editMessage(":x: Pesquisa cancelada!").setEmbeds().queue()
        }, 20000L)

        listener = ctx.jda.listener<GuildMessageReceivedEvent> {
            if (it.author != ctx.author || it.channel != ctx.channel) return@listener
            val id = it.message.contentRaw.toIntOrNull()

            if (id == null || id < 0 || id > 10) {
                ctx.channel.sendMessage(":x: Número inválido!").queue()
                return@listener
            }

            timer.cancel()
            ctx.jda.removeEventListener(listener)
            pendingSearches.remove(ctx.author.idLong)

            if (id == 0) {
                msg.editMessage(":x: Pesquisa cancelada!").setEmbeds().queue()
                return@listener
            }
            msg.delete().queue()

            if (!Utils.canPlay(ctx.selfMember, ctx.member, ctx.channel)) return@listener

            val track = tracks[id-1]

            if (!ctx.selfMember.voiceState!!.inVoiceChannel()) {
                ctx.guild.audioManager.isSelfDeafened = true
                ctx.guild.audioManager.openAudioConnection(ctx.member.voiceState?.channel)
            }

            val musicManager = PlayerManager.getMusicManager(ctx.guild, ctx.channel)
            musicManager.scheduler.queue(track, ctx.member)
            if (musicManager.scheduler.queue.isNotEmpty()) {
                ctx.channel.sendMessage(":bookmark_tabs: Adicionado à lista `${track.info.title}`").queue()
            }
        }
    }
}