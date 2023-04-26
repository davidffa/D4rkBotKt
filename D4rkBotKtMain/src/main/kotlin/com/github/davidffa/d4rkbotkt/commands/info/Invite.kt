package com.github.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.messages.Embed
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.time.Instant

class Invite : Command(
  "invite",
  listOf("inv", "convite"),
  category = "Info",
  botPermissions = listOf(Permission.MESSAGE_SEND)
) {
  override suspend fun run(ctx: CommandContext) {
    val selfID = ctx.selfUser.id

    if (ctx.selfMember.getPermissions(ctx.channel).contains(Permission.MESSAGE_EMBED_LINKS)) {
      val embed = Embed {
        title = ctx.t("commands.invite.title")
        color = Utils.randColor()
        description =
          "${ctx.t("commands.invite.desctitle")}\n\n" +
                  "[${ctx.t("commands.invite.admin")}](https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot+applications.commands&permissions=8)\n" +
                  "[${ctx.t("commands.invite.needed")}](https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot+applications.commands&permissions=1345711190)\n" +
                  "[${ctx.t("commands.invite.withoutperms")}](https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot+applications.commands&permissions=0)\n" +
                  "[${ctx.t("commands.invite.withoutslash")}](https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot&permissions=0)\n\n" +
                  "[${ctx.t("commands.invite.support")}](https://discord.gg/dBQnxVCTEw)"
        timestamp = Instant.now()
        footer {
          name = ctx.author.asTag
          iconUrl = ctx.author.effectiveAvatarUrl
        }
      }

      ctx.channel.sendMessageEmbeds(embed).queue()
      return
    }

    ctx.channel.sendMessage(
      "\${ctx.t(\"commands.invite.desctitle\")\n\n" +
              "${ctx.t("commands.invite.admin")} -> https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot+applications.commands&permissions=8\n" +
              "${ctx.t("commands.invite.needed")} -> https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot+applications.commands&permissions=1345711190\n" +
              "${ctx.t("commands.invite.withoutperms")} -> https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot+applications.commands&permissions=0\n" +
              "${ctx.t("commands.invite.withoutslash")} -> https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot&permissions=0\n\n" +
              "${ctx.t("commands.invite.support")} -> https://discord.gg/dBQnxVCTEw"
    ).queue()
  }
}