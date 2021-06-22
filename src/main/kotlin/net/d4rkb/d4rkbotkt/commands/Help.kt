package net.d4rkb.d4rkbotkt.commands

import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.d4rkb.d4rkbotkt.command.CommandManager
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import java.time.Instant

class Help(
    private val manager: CommandManager
) : Command(
    "help",
    "Mostra a lista de comandos do bot.",
    aliases = listOf("ajuda", "comandos", "commands", "cmds"),
    category = "Others",
    botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
    cooldown = 6
) {
    override fun run(ctx: CommandContext) {
        val args = ctx.args
        val channel = ctx.channel

        if (args.isEmpty()) {
            val commands = manager.commands

            val dev = ArrayList<String>()
            val info = ArrayList<String>()
            val music = ArrayList<String>()
            val others = ArrayList<String>()

            commands.forEach { cmd ->
                when (cmd.category) {
                    "Dev" -> dev.add(cmd.name)
                    "Info" -> info.add(cmd.name)
                    "Music" -> music.add(cmd.name)
                    "Others" -> others.add(cmd.name)
                    else -> others.add(cmd.name)
                }
            }

            val embed = EmbedBuilder()
                .setTitle("Ajuda")
                .setColor(Utils.randColor())
                .setFooter(ctx.author.asTag, ctx.author.effectiveAvatarUrl)
                .setTimestamp(Instant.now())

            if (ctx.author.id == "334054158879686657") {
                embed.addField("> <:kotlin:856168010004037702> Desenvolvedor [${dev.size}]",
                    "```\n${dev.joinToString(" | ")}\n```", false)
            }
            embed.addField("> :information_source: Informação [${info.size}]",
                "```\n${info.joinToString(" | ")}\n```", false)
                .addField("> <a:disco:803678643661832233> Música [${music.size}]",
                "```\n${music.joinToString(" | ")}\n```", false)
                .addField("> :books: Outros [${others.size}]",
                    "```\n${others.joinToString(" | ")}\n```", false)

            channel.sendMessageEmbeds(embed.build()).queue()
            return
        }

        val search = args[0]
        val command = manager.getCommand(search)

        if (command == null || (command.category == "Dev" && ctx.author.id != "334054158879686657")) {
            channel.sendMessage(":x: Comando não encontrado!")
            return
        }

        val desc = listOf(
            "**Nome:** ${command.name}",
            "**Descrição:** ${command.description}",
            "**Alternativas:** ${if (command.aliases.isNotEmpty()) command.aliases.joinToString(", ") else "Nenhuma"}",
            "**Cooldown:** ${command.cooldown} segundo(s)",
            "**Permissões do bot:** ${if (command.botPermissions.isNotEmpty()) Utils.translatePermissions(command.botPermissions).joinToString(", ") else "Nenhuma"}",
            "**Permissões de utilizador:** ${if (command.userPermissions.isNotEmpty()) Utils.translatePermissions(command.userPermissions).joinToString(", ") else "Nenhuma"}"
        )

        val embed = EmbedBuilder()
            .setTitle("Ajuda do comando ${args.joinToString("")}")
            .setColor(Utils.randColor())
            .setDescription(desc.joinToString("\n"))
            .setFooter(ctx.author.asTag, ctx.author.effectiveAvatarUrl)
            .setTimestamp(Instant.now())
            .build()

        channel.sendMessageEmbeds(embed).queue()
    }
}