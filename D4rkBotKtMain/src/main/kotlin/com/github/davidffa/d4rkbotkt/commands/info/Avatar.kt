package com.github.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.messages.Embed
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.time.Instant

class Avatar : Command(
  "avatar",
  listOf("av"),
  "Info",
  botPermissions = listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS),
  cooldown = 3
) {
  override suspend fun run(ctx: CommandContext) {
    val user = if (ctx.args.isEmpty()) ctx.author
    else Utils.findUser(ctx.args.joinToString(" "), ctx.guild)

    if (user == null) {
      ctx.channel.sendMessage(ctx.t("errors.user.notfound")).queue()
      return
    }

    val url = "${user.effectiveAvatarUrl}?size=4096"

    val embed = Embed {
      title = ctx.t("commands.avatar.title", listOf(user.name))
      description = ctx.t("commands.avatar.description", listOf(url))
      color = Utils.randColor()
      image = url
      footer {
        name = ctx.author.name
        iconUrl = ctx.author.effectiveAvatarUrl
      }
      timestamp = Instant.now()
    }

    ctx.channel.sendMessageEmbeds(embed).queue()
  }
}
