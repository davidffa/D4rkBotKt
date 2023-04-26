package com.github.davidffa.d4rkbotkt.commands.settings

import com.mongodb.client.model.Updates
import com.github.davidffa.d4rkbotkt.D4rkBot
import com.github.davidffa.d4rkbotkt.Database
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.database.GuildDB
import net.dv8tion.jda.api.Permission

class Setprefix : Command(
  "setprefix",
  listOf("prefix", "prefixo", "setarprefixo", "setprefixo"),
  "Settings",
  listOf(Permission.MESSAGE_SEND),
  listOf(Permission.MANAGE_SERVER),
  1,
  5
) {
  override suspend fun run(ctx: CommandContext) {
    val prefix = ctx.args.joinToString(" ")

    if (prefix.length > 5) {
      ctx.channel.sendMessage(ctx.t("commands.setprefix.maxLength")).queue()
      return
    }

    D4rkBot.guildCache[ctx.guild.idLong]!!.prefix = prefix
    val updated = Database.guildDB.updateOneById(ctx.guild.id, Updates.set("prefix", prefix))

    if (updated.matchedCount == 0L) {
      Database.guildDB.insertOne(GuildDB(ctx.guild.id, prefix))
    }

    ctx.channel.sendMessage(ctx.t("commands.setprefix.success", listOf(prefix))).queue()
  }
}