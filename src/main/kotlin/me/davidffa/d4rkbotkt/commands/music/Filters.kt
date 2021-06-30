package me.davidffa.d4rkbotkt.commands.music

import dev.minn.jda.ktx.CoroutineEventListener
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.onButton
import me.davidffa.d4rkbotkt.audio.GuildMusicManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.audio.filters.Filter
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
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

        val bassButton = Button.success("$nonce:bass", Emoji.fromUnicode("1️⃣"))
        val ncButton = Button.success("$nonce:nightcore", Emoji.fromUnicode("2️⃣"))
        val popButton = Button.success("$nonce:pop", Emoji.fromUnicode("3️⃣"))
        val softButton = Button.success("$nonce:soft", Emoji.fromUnicode("4️⃣"))
        val trebleButton = Button.success("$nonce:treble", Emoji.fromUnicode("5️⃣"))
        val vaporwaveButton = Button.success("$nonce:vaporwave", Emoji.fromUnicode("6️⃣"))
        val vocalsButton = Button.success("$nonce:vocals", Emoji.fromUnicode("7️⃣"))
        val eightd = Button.success("$nonce:8d", Emoji.fromUnicode("8️⃣"))
        val clearButton = Button.danger("$nonce:clear", Emoji.fromUnicode("\uD83D\uDDD1️"))
        val deleteButton = Button.danger("$nonce:close", "Fechar")

        val embed = EmbedBuilder()
            .setTitle(":level_slider: Mesa de DJ")
            .setDescription(getDescription(manager.filters))
            .setColor(Utils.randColor())
            .setThumbnail("https://i.pinimg.com/564x/a3/a9/29/a3a929cc8d09e88815b89bc071ff4d8d.jpg")
            .setFooter(ctx.author.asTag, ctx.author.effectiveAvatarUrl)
            .setTimestamp(Instant.now())

        val controls = listOf(
            ActionRow.of(bassButton, ncButton, popButton, softButton, clearButton),
            ActionRow.of(trebleButton, vaporwaveButton, vocalsButton, eightd, deleteButton)
        )

        val msg = ctx.channel.sendMessageEmbeds(embed.build())
            .setActionRows(controls)
            .await()

        val listeners = ArrayList<CoroutineEventListener>()
        manager.djtableMessage = msg

        val timer = Timer()
        timer.schedule(timerTask {
            close(ctx.jda, listeners, manager)
        }, 60000)

        val bassListener = ctx.jda.onButton("$nonce:bass") {
            if (it.member?.idLong != ctx.member.idLong) return@onButton
            manager.switchFilter(Filter.BASS)
            editMessage(it, embed, manager.filters)
        }
        val ncListener = ctx.jda.onButton("$nonce:nightcore") {
            if (it.member?.idLong != ctx.member.idLong) return@onButton
            manager.switchFilter(Filter.NIGHTCORE)
            editMessage(it, embed, manager.filters)
        }
        val popListener = ctx.jda.onButton("$nonce:pop") {
            if (it.member?.idLong != ctx.member.idLong) return@onButton
            manager.switchFilter(Filter.POP)
            editMessage(it, embed, manager.filters)
        }
        val softListener = ctx.jda.onButton("$nonce:soft") {
            if (it.member?.idLong != ctx.member.idLong) return@onButton
            manager.switchFilter(Filter.SOFT)
            editMessage(it, embed, manager.filters)
        }
        val trebleListener = ctx.jda.onButton("$nonce:treble") {
            if (it.member?.idLong != ctx.member.idLong) return@onButton
            manager.switchFilter(Filter.TREBLEBASS)
            editMessage(it, embed, manager.filters)
        }
        val vaporwaveListener = ctx.jda.onButton("$nonce:vaporwave") {
            if (it.member?.idLong != ctx.member.idLong) return@onButton
            manager.switchFilter(Filter.VAPORWAVE)
            editMessage(it, embed, manager.filters)
        }
        val vocalsListener = ctx.jda.onButton("$nonce:vocals") {
            if (it.member?.idLong != ctx.member.idLong) return@onButton
            manager.switchFilter(Filter.VOCALS)
            editMessage(it, embed, manager.filters)
        }
        val eightdListener = ctx.jda.onButton("$nonce:8d") {
            if (it.member?.idLong != ctx.member.idLong) return@onButton
            manager.switchFilter(Filter.EIGHTD)
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

        listeners.add(bassListener)
        listeners.add(ncListener)
        listeners.add(popListener)
        listeners.add(softListener)
        listeners.add(trebleListener)
        listeners.add(vaporwaveListener)
        listeners.add(vocalsListener)
        listeners.add(eightdListener)
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

    private fun editMessage(event: ButtonClickEvent, embed: EmbedBuilder, filters: List<Filter>) {
        embed.setDescription(getDescription(filters))
        event.editMessageEmbeds(embed.build())
            .queue()
    }

    private fun getDescription(filters: List<Filter>): String {
        return  "**1)** Bass ${if (filters.contains(Filter.BASS)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "**2)** Nightcore ${if (filters.contains(Filter.NIGHTCORE)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "**3)** Pop ${if (filters.contains(Filter.POP)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "**4)** Soft ${if (filters.contains(Filter.SOFT)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "**5)** Treblebass ${if (filters.contains(Filter.TREBLEBASS)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "**6)** Vaporwave ${if (filters.contains(Filter.VAPORWAVE)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "**7)** Vocals ${if (filters.contains(Filter.VOCALS)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n" +
                "**8)** 8D ${if (filters.contains(Filter.EIGHTD)) "<:on:764478511875751937>" else "<:off:764478504124416040>"}\n\n" +
                ":wastebasket: - Remove todos os filtros"
    }
}