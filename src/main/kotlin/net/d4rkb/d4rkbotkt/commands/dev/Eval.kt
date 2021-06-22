package net.d4rkb.d4rkbotkt.commands.dev

import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import org.openjdk.nashorn.api.scripting.NashornException
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.lang.Exception

class Eval: Command(
    "eval",
    "Executa um código e retorna o seu resultado.",
    aliases = listOf("e", "evl", "evaluate"),
    "<código>",
    "Dev",
    botPermissions = listOf(Permission.MESSAGE_WRITE),
    cooldown = 0,
    dm = true
) {
    private val engine = NashornScriptEngineFactory().scriptEngine

    init {
        try {
            engine.eval(
                "var imports = new JavaImporter(" +
                        "java.io," +
                        "java.lang," +
                        "java.util," +
                        "Packages.net.dv8tion.jda.api," +
                        "Packages.net.dv8tion.jda.api.entities," +
                        "Packages.net.dv8tion.jda.api.entities.impl," +
                        "Packages.net.dv8tion.jda.api.managers," +
                        "Packages.net.dv8tion.jda.api.managers.impl," +
                        "Packages.net.dv8tion.jda.api.utils);"
            )
        } catch (e: NashornException) {
            e.printStackTrace()
        }
    }

    override fun run(ctx: CommandContext) {
        if (ctx.author.id != "334054158879686657") {
            ctx.channel.sendMessage(":x: Sem permissão!").queue()
            return
        }

        try {
            engine.put("event", ctx.event)
            engine.put("message", ctx.message)
            engine.put("channel", ctx.channel)
            engine.put("args", ctx.args)
            engine.put("jda", ctx.jda)

            if (ctx.channel.type == ChannelType.TEXT) {
                engine.put("guild", ctx.guild)
                engine.put("member", ctx.event.member)
            }

            val out = engine.eval(
                "(function() {" +
                        "with (imports) {" +
                        "return ${ctx.args.joinToString(" ")}" +
                        "}" +
                        "})();"
            )

            ctx.channel.sendMessage(":outbox_tray: **Output:**\n```kt\n${out}\n```").queue()
        }catch (e: Exception) {
            ctx.channel.sendMessage(":x: **Erro:**```kt\n${e}\n```").queue()
        }
    }
}