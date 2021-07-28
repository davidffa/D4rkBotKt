package me.davidffa.d4rkbotkt.commands.others

import dev.minn.jda.ktx.EmbedBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.time.Instant

class Github : Command(
  "github",
  "Informações sobre algum perfil do github.",
  usage = "<Nome>",
  botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
  cooldown = 5,
  category = "Others",
  args = 1
) {
  override suspend fun run(ctx: CommandContext) {
    val req = Request.Builder()
      .url("https://api.github.com/users/${ctx.args[0]}")
      .build()

    val res = D4rkBot.okHttpClient.newCall(req).await()

    if (res.code() != 200) {
      ctx.channel.sendMessage(":x: Perfil não encontrado!").queue()
      return
    }

    val user = DataObject.fromJson(withContext(Dispatchers.IO) { res.body()!!.string() })
    res.close()

    val embed = EmbedBuilder {
      title = "<:github:784791056670654465> Perfil de ${user.getString("login")}"
      url = "https://github.com/${user.getString("login")}"
      if (!user.isNull("bio")) description = "```\n${user.getString("bio")}```"
      field {
        name = ":bust_in_silhouette: Nome"
        value = if (!user.isNull("name")) user.getString("name") else user.getString("login")
      }
      field {
        name = ":id: ID"
        value = user.getString("id")
      }
      field {
        name = ":open_file_folder: Repositórios públicos"
        value = user.getString("public_repos")
      }
      field {
        name = "<:followers:784795303156908032> Seguidores"
        value = user.getString("followers")
      }
      field {
        name = ":busts_in_silhouette: A seguir"
        value = user.getString("following")

      }
      color = Utils.randColor()
      thumbnail = "${user.getString("avatar_url")}${(1..10000).random()}"
      footer {
        name = ctx.author.asTag
        iconUrl = ctx.author.effectiveAvatarUrl
      }
      timestamp = Instant.now()
    }

    if (!user.isNull("email")) {
      embed.field {
        name = ":e_mail: Email"
        value = user.getString("email")
      }
    }

    if (!user.isNull("company")) {
      embed.field {
        name = ":classical_building: Empresa"
        value = user.getString("company")
      }
    }

    if (!user.isNull("twitter_username")) {
      embed.field {
        name = ":e_mail: Email"
        value =
          "[@${user.getString("twitter_username")}](https://twitter.com/${user.getString("twitter_username")})"
      }
    }

    if (!user.isNull("location")) {
      embed.field {
        name = ":map: Localização"
        value = user.getString("location")
      }
    }

    embed.field {
      name = ":calendar: Criado em"
      value = "<t:${Instant.parse(user.getString("created_at")).epochSecond}:d> (<t:${
        Instant.parse(user.getString("created_at")).epochSecond
      }:R>)"
    }

    embed.field {
      name = ":calendar: Atualizado em"
      value = "<t:${Instant.parse(user.getString("updated_at")).epochSecond}:d> (<t:${
        Instant.parse(user.getString("updated_at")).epochSecond
      }:R>)"
    }

    ctx.channel.sendMessageEmbeds(embed.build()).queue()
  }
}