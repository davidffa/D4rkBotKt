package me.davidffa.d4rkbotkt.commands.settings

import com.mongodb.client.model.Updates
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role

class Djrole : Command(
    "djrole",
    "Seta o cargo de DJ.",
    listOf("dj", "cargodj"),
    "[Cargo/0]",
    "Settings",
    listOf(Permission.MESSAGE_WRITE),
    cooldown = 5
) {
    override suspend fun run(ctx: CommandContext) {
        val cache = D4rkBot.guildCache[ctx.guild.idLong]!!
        val djRole = cache.djRole

        if (ctx.args.isEmpty()) {
            if (djRole == null) {
                ctx.channel.sendMessage(":x: Nenhum cargo de DJ setado. **Usa:** `${ctx.prefix}djrole <Cargo>` para setar um cargo de DJ.").queue()
                return
            }
            val role = ctx.guild.roleCache.getElementById(djRole)

            if (role == null) {
                cache.djRole = null
                Database.guildDB.updateOneById(ctx.guild.id, Updates.set("djRole", null))
                ctx.channel.sendMessage(":x: O cargo de DJ antigo foi apagado! **Usa:** `${ctx.prefix}djrole <Cargo>` para setar um novo cargo de DJ.").queue()
                return
            }

            ctx.channel.sendMessage("<a:disco:803678643661832233> Cargo de DJ atual: `${role.name}`\n**Usa:** `${ctx.prefix}djrole <Cargo> (0 para desativar)`").queue()
            return
        }

        if (!ctx.member.permissions.contains(Permission.MANAGE_SERVER) && ctx.member.id != "334054158879686657") {
            ctx.channel.sendMessage(":x: Precisas da permissão `Gerenciar Cargos` para usar este comando.").queue()
            return
        }

        if (ctx.args[0] == "0") {
            if (djRole == null) {
                ctx.channel.sendMessage(":x: O cargo de DJ não está ativo!").queue()
                return
            }

            cache.djRole = null
            Database.guildDB.updateOneById(ctx.guild.id, Updates.set("djRole", null))
            ctx.channel.sendMessage("<a:disco:803678643661832233> Cargo de DJ desativado. **Usa:** `${ctx.prefix}djrole <Cargo>` para setar um novo cargo de DJ.").queue()
            return
        }

        var newRole: Role? = null

        if (ctx.message.mentionedRoles.isNotEmpty()) newRole = ctx.message.mentionedRoles[0]
        else {
            val roleById = ctx.guild.roles.find { it.id == ctx.args[0] }
            if (roleById != null) newRole = roleById
            else {
                val roleByName = ctx.guild.roles.find { it.name == ctx.args.joinToString(" ") }
                if (roleByName != null) newRole = roleByName
                else {
                    val roleBySearch = ctx.guild.roles.find { it.name.contains(ctx.args.joinToString(" ").lowercase()) }
                    if (roleBySearch != null) newRole = roleBySearch
                }
            }
        }

        if (newRole == null) {
            ctx.channel.sendMessage(":x: Cargo não encontrado!").queue()
            return
        }

        cache.djRole = newRole.id
        Database.guildDB.updateOneById(ctx.guild.id, Updates.set("djRole", newRole.id))

        ctx.channel.sendMessage("<a:disco:803678643661832233> Cargo `${newRole.name}` setado como cargo de DJ!").queue()
    }
}