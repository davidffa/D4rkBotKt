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
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import kotlin.concurrent.timerTask

class Logs : Command(
  "logs",
  "Configura os canais onde irei enviar as logs (mensagem bem-vindo etc).",
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

    val embed = generateEmbed(ctx.author, guildData, welcomeChat, memberRemoveChat)

    val nonceBytes = ByteArray(24)
    SecureRandom().nextBytes(nonceBytes)
    val nonce = Base64.getEncoder().encodeToString(nonceBytes)

    val menu = SelectionMenu("$nonce:logs", "Escolhe o tipo de logs a configurar") {
      option("Mensagens de bem-vindo", "welcome", emoji = Emoji.fromUnicode("\uD83D\uDC4B"))
      option("Mensagens de saída", "leave", emoji = Emoji.fromUnicode("\uD83D\uDEAA"))
    }

    val descEmbed = Embed {
      title = ":gear: Configuração de logs"
      color = Utils.randColor()
      description = ":white_check_mark: - Ativar\n" +
              ":x: - Desativar\n" +
              "<:chat:804050576647913522> - Escolher o canal"
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
      mainMessage.editMessage(":warning: Acabou o tempo!\nUsa o comando novamente para continuar a configurar as logs!").setEmbeds().setActionRows().queue()
    }, 5 * 60 * 1000L)

    val menuListener = ctx.jda.onSelection("$nonce:logs") {
      if (it.member != ctx.member) {
        it.reply(":x: Não podes interagir aqui!\n**Usa:** `${ctx.prefix}${name}` para poderes interagir.").setEphemeral(true).queue()
        return@onSelection
      }

      val selected = it.selectedOptions!!.first().value

      if (selected == "welcome") logType = LogType.WELCOME
      else if (selected == "leave") logType = LogType.REMOVE

      it.editMessageEmbeds(descEmbed).setActionRows(buttons).queue()
    }

    val onButtonListener = ctx.jda.onButton("$nonce:on") {
      if (it.member != ctx.member) {
        it.reply(":x: Não podes interagir aqui!\n**Usa:** `${ctx.prefix}${name}` para poderes interagir.").setEphemeral(true).queue()
        return@onButton
      }

      when (logType) {
        LogType.WELCOME -> {
          if (guildData.welcomeMessagesEnabled == null || guildData.welcomeMessagesEnabled == false) {
            guildData.welcomeMessagesEnabled = true
            Database.guildDB.updateOneById(ctx.guild.id, Updates.set("welcomeMessagesEnabled", true))
          }
          it.editMessageEmbeds(generateEmbed(ctx.author, guildData, welcomeChat, memberRemoveChat)).setActionRow(menu).queue()
        }
        LogType.REMOVE -> {
          if (guildData.memberRemoveMessagesEnabled == null || guildData.memberRemoveMessagesEnabled == false) {
            guildData.memberRemoveMessagesEnabled = true
            Database.guildDB.updateOneById(ctx.guild.id, Updates.set("memberRemoveMessagesEnabled", true))
          }
          it.editMessageEmbeds(generateEmbed(ctx.author, guildData, welcomeChat, memberRemoveChat)).setActionRow(menu).queue()
        }
      }
    }

    val offButtonListener = ctx.jda.onButton("$nonce:off") {
      if (it.member != ctx.member) {
        it.reply(":x: Não podes interagir aqui!\n**Usa:** `${ctx.prefix}${name}` para poderes interagir.").setEphemeral(true).queue()
        return@onButton
      }

      when (logType) {
        LogType.WELCOME -> {
          if (guildData.welcomeMessagesEnabled == true) {
            guildData.welcomeMessagesEnabled = false
            Database.guildDB.updateOneById(ctx.guild.id, Updates.set("welcomeMessagesEnabled", false))
          }
          it.editMessageEmbeds(generateEmbed(ctx.author, guildData, welcomeChat, memberRemoveChat)).setActionRow(menu).queue()
        }
        LogType.REMOVE -> {
          if (guildData.memberRemoveMessagesEnabled == true) {
            guildData.memberRemoveMessagesEnabled = false
            Database.guildDB.updateOneById(ctx.guild.id, Updates.set("memberRemoveMessagesEnabled", false))
          }
          it.editMessageEmbeds(generateEmbed(ctx.author, guildData, welcomeChat, memberRemoveChat)).setActionRow(menu).queue()
        }
      }
    }

    val channelButtonListener = ctx.jda.onButton("$nonce:channel") {
      if (it.member != ctx.member) {
        it.reply(":x: Não podes interagir aqui!\n**Usa:** `${ctx.prefix}${name}` para poderes interagir.").setEphemeral(true).queue()
        return@onButton
      }

      globalTimer.cancel()

      val type = when (logType) {
        LogType.WELCOME -> "bem-vindo"
        LogType.REMOVE -> "saída"
      }

      val msg = it.editMessage("Escreve o canal ou ID do canal para setar as mensagens de $type").setEmbeds().setActionRows().await()

      var msgListener: CoroutineEventListener? = null

      val timerMsg = Timer()

      timerMsg.schedule(timerTask {
        ctx.jda.removeEventListener(msgListener)
        removeListeners(listeners, ctx.jda)

        msg.editOriginal(":warning: Acabou o tempo!\nUsa o comando novamente para continuar a configurar as logs!").queue()
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
          mainMessage.editMessage(":warning: Acabou o tempo!\nUsa o comando novamente para continuar a configurar as logs!").setEmbeds().setActionRows().queue()
        }, 5 * 60 * 1000L)

        msg.editOriginal("").setEmbeds(generateEmbed(ctx.author, guildData, welcomeChat, memberRemoveChat)).setActionRow(menu).queue()
      }
    }

    listeners.add(menuListener)
    listeners.add(onButtonListener)
    listeners.add(offButtonListener)
    listeners.add(channelButtonListener)
  }

  private fun generateEmbed(author: User, guildData: GuildCache, welcomeChat: TextChannel?, memberRemoveChat: TextChannel?): MessageEmbed {
    return Embed {
      title = ":gear: Configuração de logs"
      color = Utils.randColor()
      field {
        name = "Mensagens de bem-vindo"
        value = "${if (guildData.welcomeMessagesEnabled == true) "Ativado <:on:764478511875751937>" else "Desativado <:off:764478504124416040>"}\n" +
                "Canal: ${welcomeChat?.asMention ?: "Nenhum"}"
        inline = false
      }
      field {
        name = "Mensagens de saída"
        value = "${if (guildData.memberRemoveMessagesEnabled == true) "Ativado <:on:764478511875751937>" else "Desativado <:off:764478504124416040>"}\n" +
                "Canal: ${memberRemoveChat?.asMention ?: "Nenhum"}"
        inline = false
      }

      timestamp = Instant.now()
      footer {
        name = author.asTag
        iconUrl = author.effectiveAvatarUrl
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