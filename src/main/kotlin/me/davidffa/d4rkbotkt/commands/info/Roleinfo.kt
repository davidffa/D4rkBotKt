package me.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.Embed
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.time.Instant

class Roleinfo : Command(
  "roleinfo",
  usage = "<Cargo/ID>",
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

    val embed = Embed {
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
        name = ctx.t("commands.roleinfo.fields.position.name")
        value = "`${ctx.guild.members.filter { it.roles.contains(role) }.size}`"
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

    ctx.channel.sendMessageEmbeds(embed).queue()
  }
}