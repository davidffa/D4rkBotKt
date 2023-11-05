package com.github.davidffa.d4rkbotkt.commands

import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.onStringSelect
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.option
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.command.CommandManager
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.exceptions.ErrorResponseException.ignore
import net.dv8tion.jda.api.requests.ErrorResponse
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import kotlin.concurrent.timerTask

class Help : Command(
  "help",
  aliases = listOf("ajuda", "comandos", "commands", "cmds"),
  category = "Others",
  botPermissions = listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS),
  cooldown = 6
) {
  override suspend fun run(ctx: CommandContext) {
    val args = ctx.args
    val channel = ctx.channel

    if (args.isEmpty()) {
      val commands = CommandManager.commands

      val dev = mutableListOf<String>()
      val info = mutableListOf<String>()
      val music = mutableListOf<String>()
      val others = mutableListOf<String>()
      val settings = mutableListOf<String>()

      commands.forEach { cmd ->
        when (cmd.category) {
          "Dev" -> dev.add(cmd.name)
          "Info" -> info.add(cmd.name)
          "Music" -> music.add(cmd.name)
          "Others" -> others.add(cmd.name)
          "Settings" -> settings.add(cmd.name)
          else -> others.add(cmd.name)
        }
      }

      val commandsSize = if (ctx.author.id == "334054158879686657") commands.size else commands.size - dev.size

      val nonceBytes = ByteArray(24)
      SecureRandom().nextBytes(nonceBytes)
      val nonce = Base64.getEncoder().encodeToString(nonceBytes)

      val menu = StringSelectMenu("$nonce:help", ctx.t("commands.help.menu.placeholder")) {
        if (ctx.author.id == "334054158879686657") {
          option(
            "Desenvolvedor",
            "dev",
            "Comandos de desenvolvedor",
            Emoji.fromFormatted("<:kotlin:856168010004037702>")
          )
        }
        option(
          ctx.t("commands.help.menu.settings.label"),
          "settings",
          ctx.t("commands.help.menu.settings.description"),
          Emoji.fromUnicode("⚙️")
        )
        option(
          ctx.t("commands.help.menu.info.label"),
          "info",
          ctx.t("commands.help.menu.info.description"),
          Emoji.fromUnicode("ℹ️")
        )
        option(
          ctx.t("commands.help.menu.music.label"),
          "music",
          ctx.t("commands.help.menu.music.description"),
          Emoji.fromFormatted("<a:disco:803678643661832233>")
        )
        option(
          ctx.t("commands.help.menu.others.label"),
          "others",
          ctx.t("commands.help.menu.others.description"),
          Emoji.fromUnicode("\uD83D\uDCDA")
        )
      }

      val msg = channel.sendMessage("\u200B").setActionRow(menu).await()

      val listener = ctx.jda.onStringSelect("$nonce:help") {
        if (it.user.idLong != ctx.author.idLong) {
          it.reply(ctx.t("errors.cannotinteract", listOf(ctx.prefix, name))).setEphemeral(true).queue()
          return@onStringSelect
        }
        val option = it.selectedOptions.first()

        val embed = EmbedBuilder {
          title = ctx.t("commands.help.title")
          description = "${ctx.t("commands.help.description")} [$commandsSize]"
          color = Utils.randColor()
          footer {
            name = ctx.author.name
            iconUrl = ctx.author.effectiveAvatarUrl
          }
          timestamp = Instant.now()
        }

        when (option?.value) {
          "dev" -> {
            embed.field {
              name = "${ctx.t("commands.help.category")} [${dev.size}]"
              value = "```\n${dev.joinToString(" | ")}\n```"
            }
          }
          "settings" -> {
            embed.field {
              name = "${ctx.t("commands.help.category")} [${settings.size}]"
              value = "```\n${settings.joinToString(" | ")}\n```"
            }
          }
          "info" -> {
            embed.field {
              name = "${ctx.t("commands.help.category")} [${info.size}]"
              value = "```\n${info.joinToString(" | ")}\n```"
              inline = false
            }
          }
          "music" -> {
            embed.field {
              name = "${ctx.t("commands.help.category")} [${music.size}]"
              value = "```\n${music.joinToString(" | ")}\n```"
              inline = false
            }
          }
          "others" -> {
            embed.field {
              name = "${ctx.t("commands.help.category")} [${others.size}]"
              value = "```\n${others.joinToString(" | ")}\n```"
              inline = false
            }
          }
        }

        val editedMenu = menu.createCopy()
        editedMenu.setDefaultOptions(listOf(editedMenu.options.find { op -> op.value == option?.value }))
        it.editMessageEmbeds(embed.build()).setActionRow(editedMenu.build()).queue()
      }

      Timer().schedule(timerTask {
        ctx.jda.removeEventListener(listener)
        msg.editMessage(ctx.t("commands.help.timeout"))
          .setEmbeds()
          .setActionRow(menu.asDisabled())
          .queue(null, ignore(ErrorResponse.UNKNOWN_MESSAGE))
      }, 90000L)
      return
    }

    val search = args[0]
    val command = CommandManager.getCommand(search, ctx.author.id)

    if (command == null) {
      channel.sendMessage(ctx.t("commands.help.notFound")).queue()
      return
    }

    val desc = listOf(
      "**${ctx.t("commands.help.desc.name")}** ${command.name}",
      "**${ctx.t("commands.help.desc.description")}** ${ctx.t("help.${command.name.lowercase()}")}",
      "**${ctx.t("commands.help.desc.aliases")}** ${if (command.aliases != null) command.aliases.joinToString(", ") else "Nenhuma"}",
      "**Cooldown:** ${command.cooldown} segundo(s)",
      "**${ctx.t("commands.help.desc.botPerms")}** ${
        if (command.botPermissions != null) Utils.translatePermissions(command.botPermissions, ctx::t)
          .joinToString(", ") else ctx.t("commands.help.none")
      }",
      "**${ctx.t("commands.help.desc.userPerms")}** ${
        if (command.userPermissions != null) Utils.translatePermissions(command.userPermissions, ctx::t)
          .joinToString(", ") else ctx.t("commands.help.none")
      }"
    )

    val embed = Embed {
      title = ctx.t("commands.help.desc.title", listOf(args.first()))
      color = Utils.randColor()
      description = desc.joinToString("\n")
      footer {
        name = ctx.author.name
        iconUrl = ctx.author.effectiveAvatarUrl
      }
      timestamp = Instant.now()
    }

    channel.sendMessageEmbeds(embed).queue()
  }
}