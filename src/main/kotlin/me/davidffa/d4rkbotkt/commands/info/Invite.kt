package me.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.Embed
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.time.Instant

class Invite : Command(
  "invite",
   "Envia o link do meu convite.",
  listOf("inv", "convite"),
  category = "Info",
  botPermissions = listOf(Permission.MESSAGE_WRITE)
) {
  override suspend fun run(ctx: CommandContext) {
    val selfID = ctx.selfUser.id

    if (ctx.selfMember.getPermissions(ctx.channel).contains(Permission.MESSAGE_EMBED_LINKS)) {
      val embed = Embed {
        title = "Convite"
        color = Utils.randColor()
        description = "<a:blobdance:804026401849475094> **Adicione-me ao seu servidor usando um dos convites abaixo**\n\n" +
                "[Com permissão de administrador](https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot+applications.commands&permissions=8)\n" +
                "[Com todas as permissões necessárias](https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot+applications.commands&permissions=1345711190)\n" +
                "[Sem permissões](https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot+applications.commands&permissions=0)\n" +
                "[Sem slash commands](https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot&permissions=0)\n\n" +
                "[Servidor de Suporte](https://discord.gg/dBQnxVCTEw)"
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
      "<a:blobdance:804026401849475094> **Adicione-me ao seu servidor usando um dos convites abaixo**\n\n" +
              "Com permissão de administrador -> https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot+applications.commands&permissions=8\n" +
              "Com todas as permissões necessárias -> https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot+applications.commands&permissions=1345711190\n" +
              "Sem permissões -> https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot+applications.commands&permissions=0\n" +
              "Sem slash commands -> https://discord.com/oauth2/authorize?client_id=${selfID}&scope=bot&permissions=0\n\n" +
              "Servidor de Suporte -> https://discord.gg/dBQnxVCTEw"
    ).queue()
  }
}