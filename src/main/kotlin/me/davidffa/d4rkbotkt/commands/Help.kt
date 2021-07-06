package me.davidffa.d4rkbotkt.commands

import dev.minn.jda.ktx.Embed
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.command.CommandManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.time.Instant

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

            val dev = ArrayList<String>()
            val info = ArrayList<String>()
            val music = ArrayList<String>()
            val others = ArrayList<String>()
            val settings = ArrayList<String>()

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

            val embed = Embed {
                title = "Ajuda"
                color = Utils.randColor()
                description = "Lista de todos os meus comandos [${commandsSize}]:"

                if (ctx.author.id == "334054158879686657") {
                    field {
                        name = "> <:kotlin:856168010004037702> Desenvolvedor [${dev.size}]"
                        value = "```\n${dev.joinToString(" | ")}\n```"
                        inline = false
                    }
                }

                field {
                    name = "> :gear: Definições [${settings.size}]"
                    value = "```\n${settings.joinToString(" | ")}\n```"
                    inline = false
                }
                field {
                    name = "> :information_source: Informação [${info.size}]"
                    value = "```\n${info.joinToString(" | ")}\n```"
                    inline = false
                }
                field {
                    name = "> <a:disco:803678643661832233> Música [${music.size}]"
                    value = "```\n${music.joinToString(" | ")}\n```"
                    inline = false
                }
                field {
                    name = "> :books: Outros [${others.size}]"
                    value = "```\n${others.joinToString(" | ")}\n```"
                    inline = false
                }
                footer {
                    name = ctx.author.asTag
                    iconUrl = ctx.author.effectiveAvatarUrl
                }
                timestamp = Instant.now()
            }

            channel.sendMessageEmbeds(embed).queue()
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