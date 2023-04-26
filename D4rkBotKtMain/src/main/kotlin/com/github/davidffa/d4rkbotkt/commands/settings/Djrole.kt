package com.github.davidffa.d4rkbotkt.commands.settings

import com.mongodb.client.model.Updates
import com.github.davidffa.d4rkbotkt.D4rkBot
import com.github.davidffa.d4rkbotkt.Database
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.database.GuildDB
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Djrole : Command(
  "djrole",
  listOf("dj", "cargodj"),
  "Settings",
  listOf(Permission.MESSAGE_SEND),
  cooldown = 5
) {
  override suspend fun run(ctx: CommandContext) {
    val cache = D4rkBot.guildCache[ctx.guild.idLong]!!
    val djRole = cache.djRole

    if (ctx.args.isEmpty()) {
      if (djRole == null) {
        ctx.channel.sendMessage(ctx.t("commands.djrole.errors.noRole", listOf(ctx.prefix)))
          .queue()
        return
      }
      val role = ctx.guild.roleCache.getElementById(djRole)

      if (role == null) {
        cache.djRole = null
        Database.guildDB.updateOneById(ctx.guild.id, Updates.set("djRole", null))
        ctx.channel.sendMessage(ctx.t("commands.djrole.errors.roleDeleted", listOf(ctx.prefix)))
          .queue()
        return
      }

      ctx.channel.sendMessage(ctx.t("commands.djrole.currentRole", listOf(role.name, ctx.prefix)))
        .queue()
      return
    }

    if (!ctx.member.permissions.contains(Permission.MANAGE_SERVER) && ctx.member.id != "334054158879686657") {
      ctx.channel.sendMessage(ctx.t("commands.djrole.errors.missingPermission")).queue()
      return
    }

    if (ctx.args[0] == "0") {
      if (djRole == null) {
        ctx.channel.sendMessage(ctx.t("commands.djrole.errors.disabled")).queue()
        return
      }

      cache.djRole = null
      Database.guildDB.updateOneById(ctx.guild.id, Updates.set("djRole", null))
      ctx.channel.sendMessage(ctx.t("commands.djrole.usage", listOf(ctx.prefix)))
        .queue()
      return
    }

    val newRole = Utils.findRole(ctx.args.joinToString(" "), ctx.guild)

    if (newRole == null) {
      ctx.channel.sendMessage(ctx.t("errors.roles.notfound")).queue()
      return
    }

    cache.djRole = newRole.id
    val update = Database.guildDB.updateOneById(ctx.guild.id, Updates.set("djRole", newRole.id))

    if (update.matchedCount == 0L) {
      Database.guildDB.insertOne(GuildDB(ctx.guild.id, djRole = newRole.id))
    }

    ctx.channel.sendMessage(ctx.t("commands.djrole.success", listOf(newRole.name))).queue()
  }
}