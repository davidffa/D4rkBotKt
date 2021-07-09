package me.davidffa.d4rkbotkt.commands.music

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import dev.minn.jda.ktx.*
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import java.security.SecureRandom
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
    override suspend fun run(ctx: CommandContext) {
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

        val embed = Embed {
            title = ":mag: Resultados da procura"
            description = tracks.mapIndexed { i, track ->
                "**${i+1}º** - [${track.info.title}](${track.info.uri})"
            }.joinToString("\n")
            color = Utils.randColor()
            footer {
                name = ctx.author.asTag
                iconUrl = ctx.author.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }

        val nonceBytes = ByteArray(24)
        SecureRandom().nextBytes(nonceBytes)
        val nonce = Base64.getEncoder().encodeToString(nonceBytes)

        val one = Button.success("$nonce:1", Emoji.fromUnicode("1️⃣"))
        val two = Button.success("$nonce:2", Emoji.fromUnicode("2️⃣"))
        val three = Button.success("$nonce:3", Emoji.fromUnicode("3️⃣"))
        val four = Button.success("$nonce:4", Emoji.fromUnicode("4️⃣"))
        val five = Button.success("$nonce:5", Emoji.fromUnicode("5️⃣"))
        val six = Button.success("$nonce:6", Emoji.fromUnicode("6️⃣"))
        val seven = Button.success("$nonce:7", Emoji.fromUnicode("7️⃣"))
        val eight = Button.success("$nonce:8", Emoji.fromUnicode("8️⃣"))
        val nine = Button.success("$nonce:9", Emoji.fromUnicode("9️⃣"))
        val ten = Button.success("$nonce:10", Emoji.fromUnicode("\uD83D\uDD1F"))
        val cancel = Button.danger("$nonce:cancel", Emoji.fromUnicode("\uD83D\uDDD1️"))

        val buttons = listOf(
            ActionRow.of(one, two, three, four, five),
            ActionRow.of(six, seven, eight, nine, ten),
            ActionRow.of(cancel)
        )

        val msg = ctx.channel.sendMessageEmbeds(embed).setActionRows(buttons).await()

        var listener: CoroutineEventListener? = null

        val timer = Timer()
        timer.schedule(timerTask {
            ctx.jda.removeEventListener(listener)
            msg.editMessage(":x: Pesquisa cancelada!").setEmbeds().setActionRows().queue()
        }, 40000L)

        listener = ctx.jda.listener<ButtonClickEvent> {
            if (it.member != ctx.member || it.channel != ctx.channel) return@listener
            if (!it.componentId.startsWith(nonce)) return@listener

            val id = it.componentId.replace("$nonce:", "")

            timer.cancel()
            ctx.jda.removeEventListener(this)

            if (id == "cancel") {
                it.editMessage(":x: Pesquisa cancelada!").setEmbeds().setActionRows().queue()
                return@listener
            }

            val index = id.toIntOrNull() ?: return@listener

            msg.delete().queue()

            if (!Utils.canPlay(ctx.selfMember, ctx.member, ctx.channel)) return@listener

            val track = tracks[index-1]

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