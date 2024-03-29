package com.github.davidffa.d4rkbotkt.commands.settings

import com.mongodb.client.model.Updates
import com.github.davidffa.d4rkbotkt.D4rkBot
import com.github.davidffa.d4rkbotkt.Database
import com.github.davidffa.d4rkbotkt.Locale
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.database.GuildDB
import net.dv8tion.jda.api.Permission

class Setlang : Command(
  "setlang",
  listOf("lang", "setlanguage"),
  "Settings",
  listOf(Permission.MESSAGE_SEND),
  listOf(Permission.MANAGE_SERVER),
  1,
  10
) {
  override suspend fun run(ctx: CommandContext) {
    when (ctx.args[0].lowercase()) {
      "pt" -> {
        D4rkBot.guildCache[ctx.guild.idLong]!!.locale = Locale.PT
        val updated = Database.guildDB.updateOneById(ctx.guild.id, Updates.set("lang", "pt"))

        if (updated.matchedCount == 0L) {
          Database.guildDB.insertOne(GuildDB(ctx.guild.id, lang = "pt"))
        }
      }
      "en" -> {
        D4rkBot.guildCache[ctx.guild.idLong]!!.locale = Locale.EN
        val updated = Database.guildDB.updateOneById(ctx.guild.id, Updates.set("lang", "en"))

        if (updated.matchedCount == 0L) {
          Database.guildDB.insertOne(GuildDB(ctx.guild.id, lang = "en"))
        }
      }

      else -> {
        ctx.channel.sendMessage(ctx.t("commands.setlang.langNotFound")).queue()
        return
      }
    }

    ctx.channel.sendMessage(ctx.t("commands.setlang.success", listOf(ctx.args[0].lowercase()))).queue()
  }
}