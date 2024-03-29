package com.github.davidffa.d4rkbotkt.commands.music

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.onButton
import dev.minn.jda.ktx.events.onStringSelect
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.Embed
import com.github.davidffa.d4rkbotkt.audio.PlayerManager
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import kotlin.concurrent.timerTask

class Search : Command(
  "search",
  listOf("procurar", "searchmusic"),
  "Music",
  listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS),
  args = 1,
  cooldown = 5
) {
  override suspend fun run(ctx: CommandContext) {
    if (!Utils.canPlay(ctx::t, ctx.selfMember, ctx.member, ctx.channel)) return

    val query = if (listOf("yt", "ytm", "sc", "ym", "od").contains(ctx.args[0].lowercase())) {
      if (ctx.args.size < 2) {
        ctx.channel.sendMessage(ctx.t("commands.search.usage", listOf(ctx.prefix))).queue()
        return
      }
      if (Utils.isUrl(ctx.args[1])) ctx.args[1]
      else "${ctx.args[0]}search:${ctx.args.subList(1, ctx.args.size).joinToString(" ")}"
    } else {
      if (Utils.isUrl(ctx.args[0])) ctx.args[0]
      else "ytsearch:${ctx.args.joinToString(" ")}"
    }

    val tracks = try {
      PlayerManager.search(query, 10)
    } catch (e: IllegalStateException) {
      ctx.channel.sendMessage(ctx.t("errors.noMatches")).queue()
      null
    } catch (e: FriendlyException) {
      ctx.channel.sendMessage(ctx.t("commands.search.errorSearching", listOf(e.message.toString()))).queue()
      null
    } ?: return

    val embed = Embed {
      title = ctx.t("commands.search.title")
      description = tracks.mapIndexed { i, track ->
        "**${i + 1}º** - [${track.info.title}](${track.info.uri})"
      }.joinToString("\n")
      color = Utils.randColor()
      footer {
        name = ctx.author.name
        iconUrl = ctx.author.effectiveAvatarUrl
      }
      timestamp = Instant.now()
    }

    val nonceBytes = ByteArray(24)
    SecureRandom().nextBytes(nonceBytes)
    val nonce = Base64.getEncoder().encodeToString(nonceBytes)

    val menu = StringSelectMenu("$nonce:search", ctx.t("commands.search.placeholder"), 1..10) {
      tracks.mapIndexed { i, track ->
        option(
          if (track.info.author.isEmpty()) "Desconhecido" else formatString(track.info.author, 50),
          "$i",
          formatString(track.info.title, 80),
          emoji = if (i == 9) Emoji.fromUnicode("\uD83D\uDD1F") else Emoji.fromUnicode("${i + 1}️⃣")
        )
      }
    }

    val cancel = Button.danger("$nonce:cancel", Emoji.fromUnicode("\uD83D\uDDD1️"))

    val buttons = listOf(
      ActionRow.of(menu),
      ActionRow.of(cancel)
    )

    val msg = ctx.channel.sendMessageEmbeds(embed).setComponents(buttons).await()

    var buttonListener: CoroutineEventListener? = null
    var menuListener: CoroutineEventListener? = null

    val timer = Timer()
    timer.schedule(timerTask {
      ctx.jda.removeEventListener(buttonListener)
      ctx.jda.removeEventListener(menuListener)
      msg.editMessage(ctx.t("commands.search.cancel")).setEmbeds().setComponents().queue()
    }, 40000L)

    menuListener = ctx.jda.onStringSelect("$nonce:search") {
      if (it.member != ctx.member) {
        it.reply(ctx.t("errors.cannotinteract", listOf(ctx.prefix, name))).setEphemeral(true).queue()
        return@onStringSelect
      }

      val ids = it.selectedOptions.map { op -> op.value.toInt() }

      timer.cancel()
      ctx.jda.removeEventListener(this)
      ctx.jda.removeEventListener(buttonListener)
      msg.delete().queue()

      if (!Utils.canPlay(ctx::t, ctx.selfMember, ctx.member, ctx.channel)) return@onStringSelect

      val chosenTracks = mutableListOf<AudioTrack>()

      ids.forEach { i ->
        chosenTracks.add(tracks[i])
      }

      if (!ctx.selfMember.voiceState!!.inAudioChannel()) {
        ctx.guild.audioManager.isSelfDeafened = true
        ctx.guild.audioManager.isSelfMuted = false
        ctx.guild.audioManager.openAudioConnection(ctx.member.voiceState?.channel)
      }

      val musicManager = PlayerManager.getMusicManager(ctx.guild, ctx.channel)

      chosenTracks.forEach { track ->
        musicManager.scheduler.queue(track, ctx.member)
      }

      val embed2 = Embed {
        title = ctx.t("commands.search.added")
        description = chosenTracks.joinToString("\n") { t -> "[${t.info.title}](${t.info.uri})" }
        color = Utils.randColor()
        footer {
          name = ctx.author.name
          iconUrl = ctx.author.effectiveAvatarUrl
        }
        timestamp = Instant.now()
      }

      ctx.channel.sendMessageEmbeds(embed2).queue()
    }

    buttonListener = ctx.jda.onButton("$nonce:cancel") {
      if (it.member != ctx.member) {
        it.reply(ctx.t("errors.cannotinteract", listOf(ctx.prefix, name))).setEphemeral(true).queue()
        return@onButton
      }

      timer.cancel()

      ctx.jda.removeEventListener(this)
      ctx.jda.removeEventListener(menuListener)

      it.editMessage(ctx.t("commands.search.cancel")).setEmbeds().setComponents().queue()
      return@onButton
    }
  }

  private fun formatString(str: String, limit: Int): String {
    if (str.length <= limit) return str
    return "${str.substring(0 until limit - 3)}..."
  }
}