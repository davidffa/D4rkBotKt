package me.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.EmbedBuilder
import dev.minn.jda.ktx.interactions.sendPaginator
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ClientType
import net.dv8tion.jda.api.entities.User.UserFlag.*
import java.time.Instant

class Userinfo : Command(
    "userinfo",
    "Informações sobre alguém.",
    listOf("ui"),
    "[nome/id]",
    "Info",
    listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
    cooldown = 5
) {
    override suspend fun run(ctx: CommandContext) {
        val user = if (ctx.args.isEmpty()) ctx.author
        else Utils.findUser(ctx.args.joinToString(" "), ctx.guild)

        if (user == null) {
            ctx.channel.sendMessage(":x: Utilizador não encontrado!").queue()
            return
        }

        val member = ctx.guild.memberCache.getElementById(user.id)

        val embed = EmbedBuilder {
            title =
                ":information_source: Informações de ${if (user.isBot) "<:bot:804028762307821578>" else ""}${member?.effectiveName ?: user.name}"
            color = Utils.randColor()
            field {
                name = ":id: ID"
                value = "`${user.id}`"
            }
            field {
                name = "<:tag:863419716029906955> Tag"
                value = "`${user.asTag}`"
            }
            field {
                name = ":man_raising_hand: Menção"
                value = user.asMention
            }
            field {
                name = ":calendar: Conta criada em"
                value = "<t:${user.timeCreated.toInstant().epochSecond}:d> (<t:${user.timeCreated.toInstant().epochSecond}:R>)"
            }
            thumbnail = "${user.effectiveAvatarUrl}?size=4096"
            footer {
                name = ctx.author.asTag
                iconUrl = ctx.author.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }

        if (member != null) {
            embed.field {
                name = ":date: Entrada no servidor"
                value = "<t:${member.timeJoined.toInstant().epochSecond}:d> (<t:${member.timeJoined.toInstant().epochSecond}:R>)"
            }
            embed.field {
                name = ":shrug: Status"
                value = when (member.onlineStatus) {
                    OnlineStatus.ONLINE -> "`Online`"
                    OnlineStatus.IDLE -> "`Ausente`"
                    OnlineStatus.DO_NOT_DISTURB -> "`Ocupado`"
                    OnlineStatus.INVISIBLE -> "`Invisível`"
                    OnlineStatus.OFFLINE -> "`Offline`"
                    OnlineStatus.UNKNOWN -> "`Desconhecido`"
                }
            }
            val onlineClients = mutableListOf<String>()

            if (member.getOnlineStatus(ClientType.DESKTOP) != OnlineStatus.OFFLINE) onlineClients.add(":computer:")
            if (member.getOnlineStatus(ClientType.MOBILE) != OnlineStatus.OFFLINE) onlineClients.add(":mobile_phone:")
            if (member.getOnlineStatus(ClientType.WEB) != OnlineStatus.OFFLINE) onlineClients.add(":globe_with_meridians:")

            if (member.activeClients.isNotEmpty()) {
                embed.field {
                    name = "Dispositivos :technologist:"
                    value = onlineClients.joinToString(" - ")
                }
            }

            val sortedMembers = ctx.guild.members.sortedBy { it.timeJoined.toInstant() }
            val pos = sortedMembers.indexOf(member) + 1

            embed.field {
                name = ":trophy: Rank de entrada"
                value = "`${pos}º/${ctx.guild.memberCount}`"
            }
        }

        embed.field {
            name = "Emblemas :medal:"
            value = if (user.flags.isEmpty()) "`Nenhum`"
                else user.flags.joinToString(" ") {
                    when (it) {
                        BUG_HUNTER_LEVEL_1 -> "<:badgebughunter:803664937016360991>"
                        STAFF -> "<:staffbadge:803667272186462258>"
                        PARTNER -> "<:partnerbadge:803667091429130260>"
                        HYPESQUAD -> "<:badgehypesquadevents:803665575703478323>"
                        HYPESQUAD_BRAVERY -> "<:badgehypebravery:803665178720731137>"
                        HYPESQUAD_BRILLIANCE -> "<:badgehypebrilliance:803665185558102017>"
                        HYPESQUAD_BALANCE -> "<:badgehypebalance:803665192310800395>"
                        EARLY_SUPPORTER -> "<:badgeearlysupporter:803665859406725121>"
                        BUG_HUNTER_LEVEL_2 -> "<:BugHunterLvl2:803665318274400256>"
                        VERIFIED_BOT -> "<:vBot1:804393321862397952><:vBot2:804393321854140440>"
                        VERIFIED_DEVELOPER -> "<:dev_badge:803665036769230899>"
                        CERTIFIED_MODERATOR -> "<:DiscordCertifiedModerator:863424954371932180>"
                        else -> ""
                    }
            }
        }

        val page1 = embed.build()

        if (member != null) {
            embed.builder.clearFields()
            embed.field {
                name = ":medal: Cargos (${member.roles.size})"
                value = if (member.roles.isEmpty()) "Nenhum"
                    else member.roles.joinToString(" ") { it.asMention }
            }

            val permissions = member.getPermissions(ctx.channel)

            embed.field {
                name = ":8ball: Permissões neste canal"
                value = "```\n${Utils.translatePermissions(permissions.toList()).joinToString(", ")}```"
                inline = false
            }
        }else {
            ctx.channel.sendMessageEmbeds(page1).queue()
            return
        }

        val page2 = embed.build()

        ctx.channel.sendPaginator(page1, page2, expireAfter = 3 * 60 * 1000L, filter = {
            if (it.user.idLong == ctx.author.idLong) return@sendPaginator true
            return@sendPaginator false
        }).queue()
    }
}