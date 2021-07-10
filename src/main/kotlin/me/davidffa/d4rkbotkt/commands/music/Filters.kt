package me.davidffa.d4rkbotkt.commands.music

import dev.minn.jda.ktx.*
import dev.minn.jda.ktx.interactions.SelectionMenu
import dev.minn.jda.ktx.interactions.option
import me.davidffa.d4rkbotkt.audio.GuildMusicManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.audio.filters.Filter
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import kotlin.concurrent.timerTask

class Filters : Command(
    "filters",
    "Adiciona filtros à música.",
    listOf("musicfilters", "filtros", "audiofilters", "djtable"),
    category = "Music",
    cooldown = 5,
    botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)
) {
    override suspend fun run(ctx: CommandContext) {
        if (!Utils.canUsePlayer(ctx.selfMember, ctx.member, ctx.channel, true)) return

        val manager = PlayerManager.getMusicManager(ctx.guild.idLong)

        if (manager.djtableMessage != null) {
            ctx.channel.sendMessage(":x: Já existe uma mesa de DJ aberta!").queue()
            return
        }

        val nonceBytes = ByteArray(24)
        SecureRandom().nextBytes(nonceBytes)
        val nonce = Base64.getEncoder().encodeToString(nonceBytes)

        val menu = SelectionMenu("$nonce:filters", "Escolhe um filtro para ativar/desativar") {
            option("Bass", "bass", emoji = Emoji.fromUnicode("1️⃣"))
            option("Daycore", "daycore", emoji = Emoji.fromUnicode("2️⃣"))
            option("Nightcore", "nightcore", emoji = Emoji.fromUnicode("3️⃣"))
            option("Pop", "pop", emoji = Emoji.fromUnicode("4️⃣"))
            option("Soft", "soft", emoji = Emoji.fromUnicode("5️⃣"))
            option("Treblebass", "treble", emoji = Emoji.fromUnicode("6️⃣"))
            option("Vaporwave", "vaporwave", emoji = Emoji.fromUnicode("7️⃣"))
            option("8D", "8d", emoji = Emoji.fromUnicode("8️⃣"))
            option("Vibrato", "vibrato", emoji = Emoji.fromUnicode("9️⃣"))
            option("Vocals", "vocals", emoji = Emoji.fromUnicode("\uD83D\uDD1F"))
            option("Karaoke", "karaoke", emoji = Emoji.fromMarkdown("<:keycap_11:862005088716193793>"))
        }

        val clearButton = Button.danger("$nonce:clear", Emoji.fromUnicode("\uD83D\uDDD1️"))
        val deleteButton = Button.danger("$nonce:close", "Fechar")

        val embed = EmbedBuilder {
            title = ":level_slider: Mesa de DJ"
            color = Utils.randColor()
            description = ":wastebasket: - Remove todos os filtros"
            thumbnail = "https://i.pinimg.com/564x/a3/a9/29/a3a929cc8d09e88815b89bc071ff4d8d.jpg"
            footer {
                name = ctx.author.asTag
                iconUrl = ctx.author.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }

        val desc = getDescription(manager.filters)
        embed.field {
            name = "\u200B"
            value = desc.first
        }
        embed.field {
            name = "\u200B"
            value = desc.second
        }

        val controls = listOf(
            ActionRow.of(menu),
            ActionRow.of(clearButton, deleteButton)
        )

        val msg = ctx.channel.sendMessageEmbeds(embed.build())
            .setActionRows(controls)
            .await()

        val listeners = mutableListOf<CoroutineEventListener>()
        manager.djtableMessage = msg

        val timer = Timer()
        timer.schedule(timerTask {
            close(ctx.jda, listeners, manager)
        }, 60000)

        val menuListener = ctx.jda.onSelection("$nonce:filters") {
            val selection = it.selectedOptions?.first()!!
            if (it.member?.idLong != ctx.member.idLong) return@onSelection

            when (selection.value) {
                "bass" -> manager.switchFilter(Filter.BASS)
                "daycore" -> manager.switchFilter(Filter.DAYCORE)
                "nightcore" -> manager.switchFilter(Filter.NIGHTCORE)
                "pop" -> manager.switchFilter(Filter.POP)
                "soft" -> manager.switchFilter(Filter.SOFT)
                "treble" -> manager.switchFilter(Filter.TREBLEBASS)
                "vaporwave" -> manager.switchFilter(Filter.VAPORWAVE)
                "8d" -> manager.switchFilter(Filter.EIGHTD)
                "vibrato" -> manager.switchFilter(Filter.VIBRATO)
                "vocals" -> manager.switchFilter(Filter.VOCALS)
                "karaoke" -> manager.switchFilter(Filter.KARAOKE)
            }

            editMessage(it, embed, manager.filters)
        }

        val clearListener = ctx.jda.onButton("$nonce:clear") {
            if (it.member?.idLong != ctx.member.idLong) return@onButton
            manager.clearFilters()
            editMessage(it, embed, manager.filters)
        }
        val closeListener = ctx.jda.onButton("$nonce:close") {
            if (it.member?.idLong != ctx.member.idLong) return@onButton
            timer.cancel()
            close(ctx.jda, listeners, manager)
            it.deferEdit().queue()
        }

        listeners.add(menuListener)
        listeners.add(clearListener)
        listeners.add(closeListener)
    }

    private fun close(jda: JDA, listeners: List<CoroutineEventListener>, manager: GuildMusicManager) {
        listeners.forEach {
            jda.removeEventListener(it)
        }

        val msg = manager.djtableMessage ?: return

        msg.editMessage("<a:disco:803678643661832233> Mesa de DJ fechada!")
            .setEmbeds(emptyList())
            .setActionRows(emptyList())
            .queue()

        manager.djtableMessage = null
    }

    private fun editMessage(event: GenericComponentInteractionCreateEvent, embed: InlineEmbed, filters: List<Filter>) {
        val desc = getDescription(filters)
        embed.builder.clearFields()
        embed.field {
            name = "\u200B"
            value = desc.first
        }
        embed.field {
            name = "\u200B"
            value = desc.second
        }
        event.editMessageEmbeds(embed.build())
            .queue()
    }

    private fun getDescription(filters: List<Filter>): Pair<String, String> {
        val first = "Bass ${if (filters.contains(Filter.BASS)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "Daycore ${if (filters.contains(Filter.DAYCORE)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "Nightcore ${if (filters.contains(Filter.NIGHTCORE)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "Pop ${if (filters.contains(Filter.POP)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "Soft ${if (filters.contains(Filter.SOFT)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "Treblebass ${if (filters.contains(Filter.TREBLEBASS)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}"

        val second = "Vaporwave ${if (filters.contains(Filter.VAPORWAVE)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "8D ${if (filters.contains(Filter.EIGHTD)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "Vibrato ${if (filters.contains(Filter.VIBRATO)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "Vocals ${if (filters.contains(Filter.VOCALS)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "Karaoke ${if (filters.contains(Filter.KARAOKE)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}"

        return Pair(first, second)
    }
}