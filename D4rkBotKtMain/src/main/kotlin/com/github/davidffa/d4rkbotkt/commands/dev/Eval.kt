package com.github.davidffa.d4rkbotkt.commands.dev

import com.github.davidffa.d4rkbotkt.audio.PlayerManager
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class Eval : Command(
  "eval",
  aliases = listOf("e", "evl", "evaluate"),
  "Dev",
  botPermissions = listOf(Permission.MESSAGE_SEND),
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
                import dev.minn.jda.ktx.messages.Embed
                import dev.minn.jda.ktx.coroutines.await
                import com.github.davidffa.d4rkbotkt.utils.Utils
                import com.github.davidffa.d4rkbotkt.D4rkBot
                import com.github.davidffa.d4rkbotkt.Database
                """.trimIndent()
      )
    }
  }

  override suspend fun run(ctx: CommandContext) {
    if (ctx.author.id != "334054158879686657") {
      ctx.channel.sendMessage(":x: Sem permissão!").queue()
      return
    }

    if (ctx.args[0].lowercase() == "gc") {
      val runtime = Runtime.getRuntime()
      val oldMem = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
      System.gc()
      val newMem = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024

      ctx.channel.sendMessage("<:ram:751468688686841986> O Garbage Collector limpou ${oldMem - newMem}MB de RAM.")
        .queue()
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
    } catch (e: Exception) {
      ctx.channel.sendMessage(":x: **Erro:**```kt\n${e.message}\n```").queue()
    }
  }
}