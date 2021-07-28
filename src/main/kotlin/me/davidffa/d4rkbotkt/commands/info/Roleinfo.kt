package me.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.Embed
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.time.Instant

class Roleinfo : Command(
  "roleinfo",
  "Informação sobre um cargo no servidor.",
  usage = "<Cargo/ID>",
  args = 1,
  botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
  category = "Info",
  cooldown = 5
) {
  override suspend fun run(ctx: CommandContext) {
    val role = Utils.findRole(ctx.args.joinToString(" "), ctx.guild)

    if (role == null) {
      ctx.channel.sendMessage(":x: Cargo não encontrado!").queue()
      return
    }

    val embed = Embed {
      title = "Informações do cargo ${role.name}"
      color = role.colorRaw
      field {
        name = ":id: ID"
        value = "`${role.id}`"
      }
      field {
        name = ":calendar: Criado em"
        value = "<t:${role.timeCreated.toEpochSecond()}:d> (<t:${role.timeCreated.toEpochSecond()}:R>)"
      }
      field {
        name = "@ Mencionável"
        value = "`${if (role.isMentionable) "Sim" else "Não"}`"
      }

      field {
        name = "@ Menção"
        value = role.asMention
      }

      field {
        name = ":military_medal: Posição"
        value = "`${role.position}`"
      }
      field {
        name = ":beginner: Separado"
        value = "`${if (role.isHoisted) "Sim" else "Não"}`"
      }
      field {
        name = ":robot: Gerenciado"
        value = "`${if (role.isManaged) "Sim" else "Não"}`"
      }
      field {
        name = ":busts_in_silhouette: Membros"
        value = "`${ctx.guild.members.filter { it.roles.contains(role) }.size}`"
      }
      field {
        name = ":8ball: Permissões"
        value =
          "```\n${Utils.translatePermissions(role.permissions.toList()).joinToString(", ")}```"
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