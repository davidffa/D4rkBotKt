package me.davidffa.d4rkbotkt.commands.settings

import com.mongodb.client.model.Updates
import dev.minn.jda.ktx.*
import dev.minn.jda.ktx.interactions.SelectionMenu
import dev.minn.jda.ktx.interactions.option
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.database.GuildCache
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import kotlin.concurrent.timerTask

class Logs : Command(
  "logs",
  listOf("logconfig", "configlogs"),
  category = "Settings",
  botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
  userPermissions = listOf(Permission.MESSAGE_MANAGE)
) {
  override suspend fun run(ctx: CommandContext) {
    val guildData = D4rkBot.guildCache[ctx.guild.idLong]!!

    var welcomeChat: TextChannel? = null
    var memberRemoveChat: TextChannel? = null

    if (guildData.welcomeChatID != null) {
      welcomeChat = ctx.guild.getTextChannelById(guildData.welcomeChatID!!)

      if (welcomeChat == null) {
        guildData.welcomeChatID = null
        guildData.welcomeMessagesEnabled = false
        Database.guildDB.updateOneById(ctx.guild.id, Updates.combine(Updates.set("welcomeChatID", null), Updates.set("welcomeMessagesEnabled", false)))
      }
    }

    if (guildData.memberRemoveChatID != null) {
      memberRemoveChat = ctx.guild.getTextChannelById(guildData.memberRemoveChatID!!)

      if (welcomeChat == null) {
        guildData.memberRemoveChatID = null
        guildData.memberRemoveMessagesEnabled = false
        Database.guildDB.updateOneById(ctx.guild.id, Updates.combine(Updates.set("memberRemoveChatID", null), Updates.set("memberRemoveMessagesEnabled", false)))
      }
    }

    val embed = generateEmbed(ctx, guildData, welcomeChat, memberRemoveChat)

    val nonceBytes = ByteArray(24)
    SecureRandom().nextBytes(nonceBytes)
    val nonce = Base64.getEncoder().encodeToString(nonceBytes)

    val menu = SelectionMenu("$nonce:logs", ctx.t("commands.logs.menu.placeholder")) {
      option(ctx.t("commands.logs.menu.welcome"), "welcome", emoji = Emoji.fromUnicode("\uD83D\uDC4B"))
      option(ctx.t("commands.logs.menu.leave"), "leave", emoji = Emoji.fromUnicode("\uD83D\uDEAA"))
    }

    val descEmbed = Embed {
      title = ctx.t("commands.logs.title")
      color = Utils.randColor()
      description = ":white_check_mark: - ${ctx.t("commands.logs.enable")}\n" +
              ":x: - ${ctx.t("commands.logs.disable")}\n" +
              "<:chat:804050576647913522> - ${ctx.t("commands.logs.channel")}"
      timestamp = Instant.now()
      footer {
        name = ctx.author.asTag
        iconUrl = ctx.author.effectiveAvatarUrl
      }
    }

    val buttons = ActionRow.of(
      Button.success("$nonce:on", Emoji.fromUnicode("✅")),
      Button.danger("$nonce:off", Emoji.fromUnicode("❌")),
      Button.primary("$nonce:channel", Emoji.fromMarkdown("<:chat:804050576647913522>"))
    )

    val mainMessage = ctx.channel.sendMessageEmbeds(embed).setActionRow(menu).await()

    lateinit var logType: LogType

    val listeners = mutableListOf<CoroutineEventListener>()

    var globalTimer = Timer()

    globalTimer.schedule(timerTask {
      removeListeners(listeners, ctx.jda)
      mainMessage.editMessage(ctx.t("commands.logs.timeout")).setEmbeds().setActionRows().queue()
    }, 3 * 60 * 1000L)

    val menuListener = ctx.jda.onSelection("$nonce:logs") {
      if (it.member != ctx.member) {
        it.reply(ctx.t("errors.cannotinteract", listOf(ctx.prefix, name))).setEphemeral(true).queue()
        return@onSelection
      }

      val selected = it.selectedOptions!!.first().value

      if (selected == "welcome") logType = LogType.WELCOME
      else if (selected == "leave") logType = LogType.REMOVE

      it.editMessageEmbeds(descEmbed).setActionRows(buttons).queue()
    }

    val onButtonListener = ctx.jda.onButton("$nonce:on") {
      if (it.member != ctx.member) {
        it.reply(ctx.t("errors.cannotinteract", listOf(ctx.prefix, name))).setEphemeral(true).queue()
        return@onButton
      }

      when (logType) {
        LogType.WELCOME -> {
          if (guildData.welcomeMessagesEnabled == null || guildData.welcomeMessagesEnabled == false) {
            guildData.welcomeMessagesEnabled = true
            Database.guildDB.updateOneById(ctx.guild.id, Updates.set("welcomeMessagesEnabled", true))
          }
          it.editMessageEmbeds(generateEmbed(ctx, guildData, welcomeChat, memberRemoveChat)).setActionRow(menu).queue()
        }
        LogType.REMOVE -> {
          if (guildData.memberRemoveMessagesEnabled == null || guildData.memberRemoveMessagesEnabled == false) {
            guildData.memberRemoveMessagesEnabled = true
            Database.guildDB.updateOneById(ctx.guild.id, Updates.set("memberRemoveMessagesEnabled", true))
          }
          it.editMessageEmbeds(generateEmbed(ctx, guildData, welcomeChat, memberRemoveChat)).setActionRow(menu).queue()
        }
      }
    }

    val offButtonListener = ctx.jda.onButton("$nonce:off") {
      if (it.member != ctx.member) {
        it.reply(ctx.t("errors.cannotinteract", listOf(ctx.prefix, name))).setEphemeral(true).queue()
        return@onButton
      }

      when (logType) {
        LogType.WELCOME -> {
          if (guildData.welcomeMessagesEnabled == true) {
            guildData.welcomeMessagesEnabled = false
            Database.guildDB.updateOneById(ctx.guild.id, Updates.set("welcomeMessagesEnabled", false))
          }
          it.editMessageEmbeds(generateEmbed(ctx, guildData, welcomeChat, memberRemoveChat)).setActionRow(menu).queue()
        }
        LogType.REMOVE -> {
          if (guildData.memberRemoveMessagesEnabled == true) {
            guildData.memberRemoveMessagesEnabled = false
            Database.guildDB.updateOneById(ctx.guild.id, Updates.set("memberRemoveMessagesEnabled", false))
          }
          it.editMessageEmbeds(generateEmbed(ctx, guildData, welcomeChat, memberRemoveChat)).setActionRow(menu).queue()
        }
      }
    }

    val channelButtonListener = ctx.jda.onButton("$nonce:channel") {
      if (it.member != ctx.member) {
        it.reply(ctx.t("errors.cannotinteract", listOf(ctx.prefix, name))).setEphemeral(true).queue()
        return@onButton
      }

      globalTimer.cancel()

      val type = when (logType) {
        LogType.WELCOME -> ctx.t("commands.logs.welcome")
        LogType.REMOVE -> ctx.t("commands.logs.leave")
      }

      val msg = it.editMessage(ctx.t("commands.logs.setChannel", listOf(type))).setEmbeds().setActionRows().await()

      var msgListener: CoroutineEventListener? = null

      val timerMsg = Timer()

      timerMsg.schedule(timerTask {
        ctx.jda.removeEventListener(msgListener)
        removeListeners(listeners, ctx.jda)

        msg.editOriginal(ctx.t("commands.logs.timeout")).queue()
      }, 30000L)

      msgListener = ctx.jda.listener<GuildMessageReceivedEvent> { e ->
        if (e.channel != ctx.channel || e.member != ctx.member) return@listener
        ctx.jda.removeEventListener(this)
        timerMsg.cancel()

        val channel = Utils.findChannel(e.message.contentRaw, ctx.guild)

        if (channel != null) {
          if (channel is TextChannel) {
            when (logType) {
              LogType.WELCOME -> {
                welcomeChat = channel
                guildData.welcomeChatID = channel.id
                Database.guildDB.updateOneById(ctx.guild.id, Updates.set("welcomeChatID", channel.id))
              }
              LogType.REMOVE -> {
                memberRemoveChat = channel
                guildData.memberRemoveChatID = channel.id
                Database.guildDB.updateOneById(ctx.guild.id, Updates.set("memberRemoveChatID", channel.id))
              }
            }
          }
        }

        globalTimer = Timer()
        globalTimer.schedule(timerTask {
          removeListeners(listeners, ctx.jda)
          mainMessage.editMessage(ctx.t("commands.logs.timeout")).setEmbeds().setActionRows().queue()
        }, 3 * 60 * 1000L)

        msg.editOriginal("").setEmbeds(generateEmbed(ctx, guildData, welcomeChat, memberRemoveChat)).setActionRow(menu).queue()
      }
    }

    listeners.add(menuListener)
    listeners.add(onButtonListener)
    listeners.add(offButtonListener)
    listeners.add(channelButtonListener)
  }

  private fun generateEmbed(ctx: CommandContext, guildData: GuildCache, welcomeChat: TextChannel?, memberRemoveChat: TextChannel?): MessageEmbed {
    return Embed {
      title = ctx.t("commands.logs.title")
      color = Utils.randColor()
      field {
        name = ctx.t("commands.logs.menu.welcome")
        value = "${if (guildData.welcomeMessagesEnabled == true) ctx.t("commands.logs.embed.welcome.enabled") else ctx.t("commands.logs.embed.welcome.disabled")}\n" +
                "${ctx.t("commands.logs.embed.channel")} ${welcomeChat?.asMention ?: ctx.t("global.none")}"
        inline = false
      }
      field {
        name = ctx.t("commands.logs.menu.leave")
        value = "${if (guildData.memberRemoveMessagesEnabled == true) ctx.t("commands.logs.embed.welcome.enabled") else ctx.t("commands.logs.embed.welcome.disabled")}\n" +
                "${ctx.t("commands.logs.embed.channel")} ${memberRemoveChat?.asMention ?: ctx.t("global.none")}"
        inline = false
      }

      timestamp = Instant.now()
      footer {
        name = ctx.author.asTag
        iconUrl = ctx.author.effectiveAvatarUrl
      }
    }
  }

  private fun removeListeners(listeners: List<CoroutineEventListener>, jda: JDA) {
    listeners.forEach { jda.removeEventListener(it) }
  }

  private enum class LogType {
    WELCOME, REMOVE
  }
}