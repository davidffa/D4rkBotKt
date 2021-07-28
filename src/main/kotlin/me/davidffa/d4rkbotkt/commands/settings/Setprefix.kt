package me.davidffa.d4rkbotkt.commands.settings

import com.mongodb.client.model.Updates
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.database.GuildDB
import net.dv8tion.jda.api.Permission

class Setprefix : Command(
  "setprefix",
  "Muda o meu prefixo no servidor.",
  listOf("prefix", "prefixo", "setarprefixo", "setprefixo"),
  "<Prefixo>",
  "Settings",
  listOf(Permission.MESSAGE_WRITE),
  listOf(Permission.MANAGE_SERVER),
  1,
  5
) {
  override suspend fun run(ctx: CommandContext) {
    val prefix = ctx.args.joinToString(" ")

    if (prefix.length > 5) {
      ctx.channel.sendMessage(":x: O meu prefixo não pode ultrapassar os 5 caracteres.").queue()
      return
    }

    D4rkBot.guildCache[ctx.guild.idLong]!!.prefix = prefix
    val updated = Database.guildDB.updateOneById(ctx.guild.id, Updates.set("prefix", prefix))

    if (updated.matchedCount == 0L) {
      Database.guildDB.insertOne(GuildDB(ctx.guild.id, prefix))
    }

    ctx.channel.sendMessage("<a:verificado:803678585008816198> O meu prefixo foi alterado para `${prefix}`").queue()
  }
}