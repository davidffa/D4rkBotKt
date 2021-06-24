package net.d4rkb.d4rkbotkt.commands.settings

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import net.d4rkb.d4rkbotkt.D4rkBot
import net.d4rkb.d4rkbotkt.Database
import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.d4rkb.d4rkbotkt.database.GuildDB
import net.dv8tion.jda.api.Permission

class Setprefix : Command(
    "setprefix",
    "Muda o meu prefixo no servidor.",
    listOf("prefix", "prefixo", "setarprefixo", "setprefixo"),
    "<Prefixo>",
    "Settings",
    listOf(Permission.MANAGE_SERVER),
    listOf(Permission.MESSAGE_WRITE),
    1,
    5
) {
    override fun run(ctx: CommandContext) {
        val prefix = ctx.args.joinToString(" ")

        if (prefix.length > 5) {
            ctx.channel.sendMessage(":x: O meu prefixo não pode ultrapassar os 5 caracteres.").queue()
            return
        }

        D4rkBot.guildCache[ctx.guild.id]!!.prefix = prefix
        val updated = Database.guildDB.findOneAndUpdate(Filters.eq("_id", ctx.guild.id), Updates.set("prefix", prefix))

        if (updated == null) {
            Database.guildDB.insertOne(GuildDB(ctx.guild.id, prefix))
        }

        ctx.channel.sendMessage("<a:verificado:803678585008816198> O meu prefixo foi alterado para `${prefix}`").queue()
    }
}