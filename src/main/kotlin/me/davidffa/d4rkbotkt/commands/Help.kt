package me.davidffa.d4rkbotkt.commands

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.EmbedBuilder
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.SelectionMenu
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.onSelection
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.command.CommandManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.exceptions.ErrorResponseException.ignore
import net.dv8tion.jda.api.requests.ErrorResponse
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import kotlin.concurrent.timerTask

class Help : Command(
    "help",
    "Mostra a lista de comandos do bot.",
    aliases = listOf("ajuda", "comandos", "commands", "cmds"),
    category = "Others",
    botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
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

            val menu = SelectionMenu("$nonce:help", "Escolhe uma categoria de comandos para ver") {
                if (ctx.author.id == "334054158879686657") {
                    option("Desenvolvedor", "dev", "Comandos de desenvolvedor", Emoji.fromMarkdown("<:kotlin:856168010004037702>"))
                }
                option("Definições", "settings", "Comandos de configuração do bot", Emoji.fromUnicode("⚙️"))
                option("Informação", "info", "Comandos de informação geral", Emoji.fromUnicode("ℹ️"))
                option("Música", "music", "Comandos de música", Emoji.fromMarkdown("<a:disco:803678643661832233>"))
                option("Outros", "others", "Outros comandos de utilidade", Emoji.fromUnicode("\uD83D\uDCDA"))
            }

            val msg = channel.sendMessage("\u200B").setActionRow(menu).await()

            val listener = ctx.jda.onSelection("$nonce:help") {
                if (it.user.idLong != ctx.author.idLong) return@onSelection
                val option = it.selectedOptions?.first()

                val embed = EmbedBuilder {
                    title = "Ajuda"
                    description = "Quantidade total de comandos [${commandsSize}]"
                    color = Utils.randColor()
                    footer {
                        name = ctx.author.asTag
                        iconUrl = ctx.author.effectiveAvatarUrl
                    }
                    timestamp = Instant.now()
                }

                when (option?.value) {
                    "dev" -> {
                        embed.field {
                            name = "> <:kotlin:856168010004037702> Desenvolvedor [${dev.size}]"
                            value = "```\n${dev.joinToString(" | ")}\n```"
                        }
                    }
                    "settings" -> {
                        embed.field {
                            name = "> :gear: Definições [${settings.size}]"
                            value = "```\n${settings.joinToString(" | ")}\n```"
                        }
                    }
                    "info" -> {
                        embed.field {
                            name = "> :information_source: Informação [${info.size}]"
                            value = "```\n${info.joinToString(" | ")}\n```"
                            inline = false
                        }
                    }
                    "music" -> {
                        embed.field {
                            name = "> <a:disco:803678643661832233> Música [${music.size}]"
                            value = "```\n${music.joinToString(" | ")}\n```"
                            inline = false
                        }
                    }
                    "others" -> {
                        embed.field {
                            name = "> :books: Outros [${others.size}]"
                            value = "```\n${others.joinToString(" | ")}\n```"
                            inline = false
                        }
                    }
                }

                it.editMessageEmbeds(embed.build()).queue()
            }

            Timer().schedule(timerTask {
                ctx.jda.removeEventListener(listener)
                msg.editMessage(":warning: O tempo expirou!\nUse o comando novamente para continuar a usar o menu!")
                    .setEmbeds()
                    .setActionRows()
                    .queue(null, ignore(ErrorResponse.UNKNOWN_MESSAGE))
            }, 90000L)
            return
        }

        val search = args[0]
        val command = CommandManager.getCommand(search, ctx.author.id)

        if (command == null) {
            channel.sendMessage(":x: Comando não encontrado!")
            return
        }

        val desc = listOf(
            "**Nome:** ${command.name}",
            "**Descrição:** ${command.description}",
            "**Alternativas:** ${if (command.aliases != null) command.aliases.joinToString(", ") else "Nenhuma"}",
            "**Cooldown:** ${command.cooldown} segundo(s)",
            "**Permissões do bot:** ${if (command.botPermissions != null) Utils.translatePermissions(command.botPermissions).joinToString(", ") else "Nenhuma"}",
            "**Permissões de utilizador:** ${if (command.userPermissions != null) Utils.translatePermissions(command.userPermissions).joinToString(", ") else "Nenhuma"}"
        )

        val embed = Embed {
            title = "Ajuda do comando ${args.joinToString("")}"
            color = Utils.randColor()
            description = desc.joinToString("\n")
            footer {
                name = ctx.author.asTag
                iconUrl = ctx.author.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }

        channel.sendMessageEmbeds(embed).queue()
    }
}