package me.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.EmbedBuilder
import dev.minn.jda.ktx.interactions.sendPaginator
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.time.Instant
import kotlin.math.min

class Roleinfo : Command(
  "roleinfo",
  args = 1,
  botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
  category = "Info",
  cooldown = 5
) {
  override suspend fun run(ctx: CommandContext) {
    val role = Utils.findRole(ctx.args.joinToString(" "), ctx.guild)

    if (role == null) {
      ctx.channel.sendMessage(ctx.t("errors.roles.notfound")).queue()
      return
    }

    val members = ctx.guild.members.filter { it.roles.contains(role) }

    val embed = EmbedBuilder {
      title = ctx.t("commands.roleinfo.title", listOf(role.name))
      color = role.colorRaw
      field {
        name = ":id: ID"
        value = "`${role.id}`"
      }
      field {
        name = ctx.t("utils.created.name")
        value = ctx.t("utils.created.value", listOf(role.timeCreated.toEpochSecond().toString()))
      }
      field {
        name = ctx.t("commands.roleinfo.fields.mentionable.name")
        value = "`${if (role.isMentionable) ctx.t("global.yes") else ctx.t("global.no")}`"
      }

      field {
        name = ctx.t("commands.roleinfo.fields.mention.name")
        value = role.asMention
      }

      field {
        name = ctx.t("commands.roleinfo.fields.position.name")
        value = "`${role.position}`"
      }
      field {
        name = ctx.t("commands.roleinfo.fields.hoist.name")
        value = "`${if (role.isHoisted) ctx.t("global.yes") else ctx.t("global.no")}`"
      }
      field {
        name = ctx.t("commands.roleinfo.fields.managed.name")
        value = "`${if (role.isManaged) ctx.t("global.yes") else ctx.t("global.no")}`"
      }
      field {
        name = ctx.t("commands.roleinfo.fields.members.name")
        value = "`${members.size}`"
      }
      field {
        name = ctx.t("commands.roleinfo.fields.permissions.name")
        value =
          "```\n${
            if (role.permissions.isEmpty()) ctx.t("commands.roleinfo.fields.permissions.value.none")
            else Utils.translatePermissions(role.permissions.toList(), ctx::t).joinToString(", ")
          }```"
        inline = false
      }
      timestamp = Instant.now()
      footer {
        name = ctx.author.asTag
        iconUrl = ctx.author.effectiveAvatarUrl
      }
    }

    val page1 = embed.build()

    embed.builder.clearFields()
    embed.description = "**${ctx.t("commands.roleinfo.fields.members.name")} [${members.size}]**\n${members.slice(0 until min(members.size, 70)).joinToString(", ") { it.asMention }}" +
            "${if (members.size > 70) "... (${ctx.t("commands.roleinfo.more", listOf((members.size - 70).toString()))})" else ""}"

    val page2 = embed.build()

    ctx.channel.sendPaginator(page1, page2, expireAfter = 3 * 60 * 1000L, filter = {
      if (it.user.idLong == ctx.author.idLong) return@sendPaginator true
      return@sendPaginator false
    }).queue()
  }
}
