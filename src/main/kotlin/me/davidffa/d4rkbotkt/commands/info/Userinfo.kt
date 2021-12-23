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
import kotlin.time.Duration.Companion.minutes

class Userinfo : Command(
  "userinfo",
  listOf("ui"),
  "Info",
  listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS),
  cooldown = 5
) {
  override suspend fun run(ctx: CommandContext) {
    val user = if (ctx.args.isEmpty()) ctx.author
    else Utils.findUser(ctx.args.joinToString(" "), ctx.guild)

    if (user == null) {
      ctx.channel.sendMessage(ctx.t("errors.user.notfound")).queue()
      return
    }

    val member = ctx.guild.memberCache.getElementById(user.id)

    val embed = EmbedBuilder {
      title = ctx.t(
        "commands.userinfo.title",
        listOf("${if (user.isBot) "<:bot:804028762307821578>" else ""}${member?.effectiveName ?: user.name}")
      )
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
        name = ctx.t("commands.userinfo.mention")
        value = user.asMention
      }
      field {
        name = ctx.t("commands.userinfo.created.name")
        value = ctx.t("utils.created.value", listOf(user.timeCreated.toEpochSecond().toString()))
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
        name = ctx.t("commands.userinfo.joined.name")
        value = ctx.t("utils.created.value", listOf(member.timeJoined.toEpochSecond().toString()))
      }
      embed.field {
        name = ":shrug: Status"
        value = when (member.onlineStatus) {
          OnlineStatus.ONLINE -> "`Online`"
          OnlineStatus.IDLE -> "`${ctx.t("commands.userinfo.status.idle")}`"
          OnlineStatus.DO_NOT_DISTURB -> "`${ctx.t("commands.userinfo.status.dnd")}`"
          OnlineStatus.INVISIBLE -> "`${ctx.t("commands.userinfo.status.invisible")}`"
          OnlineStatus.OFFLINE -> "`Offline`"
          OnlineStatus.UNKNOWN -> "`${ctx.t("global.unknown")}`"
        }
      }
      val onlineClients = mutableListOf<String>()

      if (member.getOnlineStatus(ClientType.DESKTOP) != OnlineStatus.OFFLINE) onlineClients.add(":computer:")
      if (member.getOnlineStatus(ClientType.MOBILE) != OnlineStatus.OFFLINE) onlineClients.add(":mobile_phone:")
      if (member.getOnlineStatus(ClientType.WEB) != OnlineStatus.OFFLINE) onlineClients.add(":globe_with_meridians:")

      if (member.activeClients.isNotEmpty()) {
        embed.field {
          name = "${ctx.t("commands.userinfo.devices")} :technologist:"
          value = onlineClients.joinToString(" - ")
        }
      }

      val sortedMembers = ctx.guild.members.sortedBy { it.timeJoined.toInstant() }
      val pos = sortedMembers.indexOf(member) + 1

      embed.field {
        name = ctx.t("commands.userinfo.joinedrank")
        value = "`${pos}º/${ctx.guild.memberCount}`"
      }
    }

    val badges = mutableListOf<String>()

    if (!user.flags.isEmpty()) user.flags.forEach {
      badges.add(
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
      )
    }

    if ((member != null && member.timeBoosted != null)) {
      badges.add("<:badgenitro:803666299556200478>")
      badges.add("<:badgebooster:803666384373809233>")
    } else if (user.avatarId?.startsWith("a_") == true)
      badges.add("<:badgenitro:803666299556200478>")


    embed.field {
      name = ctx.t("commands.userinfo.badges")
      value = if (badges.isEmpty()) "`${ctx.t("global.none")}`" else badges.joinToString(" ")
    }

    val page1 = embed.build()

    if (member != null) {
      embed.builder.clearFields()
      embed.field {
        name = ":medal: ${ctx.t("commands.userinfo.roles")} (${member.roles.size})"
        value = if (member.roles.isEmpty()) ctx.t("global.none")
        else member.roles.joinToString(" ") { it.asMention }
      }

      val permissions = member.getPermissions(ctx.channel)

      embed.field {
        name = ctx.t("commands.userinfo.permissions.name")
        value = "```\n${
          if (permissions.isEmpty()) ctx.t("commands.userinfo.permissions.none")
          else Utils.translatePermissions(permissions.toList(), ctx::t).joinToString(", ")
        }```"
        inline = false
      }
    } else {
      ctx.channel.sendMessageEmbeds(page1).queue()
      return
    }

    val page2 = embed.build()

    ctx.channel.sendPaginator(page1, page2, expireAfter = 3.minutes, filter = {
      if (it.user.idLong == ctx.author.idLong) return@sendPaginator true
      return@sendPaginator false
    }).queue()
  }
}