package me.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.Embed
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.time.Instant

class Avatar : Command(
  "avatar",
  "Mostra o teu avatar ou de outra pessoa em uma imagem grande.",
  listOf("av"),
  "[ID/Nome]",
  "Info",
  botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
  cooldown = 3
) {
  override suspend fun run(ctx: CommandContext) {
    val user = if (ctx.args.isEmpty()) ctx.author
    else Utils.findUser(ctx.args.joinToString(" "), ctx.guild)

    if (user == null) {
      ctx.channel.sendMessage(":x: Utilizador não encontrado!").queue()
      return
    }

    val url = "${user.effectiveAvatarUrl}?size=4096"

    val embed = Embed {
      title = ":frame_photo: Avatar de ${user.name}#${user.discriminator}"
      description = ":diamond_shape_with_a_dot_inside: Clique [aqui](${url}) para baixar a imagem!"
      color = Utils.randColor()
      image = url
      footer {
        name = ctx.author.asTag
        iconUrl = ctx.author.effectiveAvatarUrl
      }
      timestamp = Instant.now()
    }

    ctx.channel.sendMessageEmbeds(embed).queue()
  }
}
