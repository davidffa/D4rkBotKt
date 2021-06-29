package me.davidffa.d4rkbotkt.commands.dev

import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.lavaplayer.PlayerManager
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class Eval: Command(
    "eval",
    "Executa um código e retorna o seu resultado.",
    aliases = listOf("e", "evl", "evaluate"),
    "<código>",
    "Dev",
    botPermissions = listOf(Permission.MESSAGE_WRITE),
    cooldown = 0,
    args = 1
) {
    private val engine: ScriptEngine by lazy {
        ScriptEngineManager().getEngineByExtension("kts")!!.apply {
            this.eval(
                """
                import net.dv8tion.jda.api.*
                import net.dv8tion.jda.api.entities.*
                import net.dv8tion.jda.api.exceptions.*
                import net.dv8tion.jda.api.utils.*
                import net.dv8tion.jda.api.requests.restaction.*
                import net.dv8tion.jda.api.requests.*
                import kotlin.collections.*
                import kotlinx.coroutines.*
                import java.util.*
                import java.util.concurrent.*
                import java.util.stream.*
                import java.io.*
                import java.time.* 
                import dev.minn.jda.ktx.Embed
                import dev.minn.jda.ktx.await
                import me.davidffa.d4rkbotkt.utils.Utils
                import me.davidffa.d4rkbotkt.D4rkBot
                import me.davidffa.d4rkbotkt.Database
                """.trimIndent()
            )
        }
    }

    override suspend fun run(ctx: CommandContext) {
        if (ctx.author.id != "334054158879686657") {
            ctx.channel.sendMessage(":x: Sem permissão!").queue()
            return
        }

        engine.put("event", ctx.event)
        engine.put("message", ctx.message)
        engine.put("channel", ctx.channel)
        engine.put("args", ctx.args)
        engine.put("jda", ctx.jda)
        engine.put("PlayerManager", PlayerManager)
        engine.put("ctx", ctx)

        if (ctx.channel.type == ChannelType.TEXT) {
            engine.put("guild", ctx.guild)
            engine.put("member", ctx.event.member)
        }

        try {
            val out = engine.eval(ctx.args.joinToString(" "))

            ctx.channel.sendMessage(":outbox_tray: **Output:**\n```kt\n${out}\n```").queue()
        }catch (e: Exception) {
            ctx.channel.sendMessage(":x: **Erro:**```kt\n${e.message}\n```").queue()
        }
    }
}