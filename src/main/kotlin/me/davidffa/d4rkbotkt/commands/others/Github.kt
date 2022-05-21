package me.davidffa.d4rkbotkt.commands.others

import dev.minn.jda.ktx.messages.EmbedBuilder
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
  botPermissions = listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS),
  cooldown = 5,
  category = "Others",
  args = 1
) {
  override suspend fun run(ctx: CommandContext) {
    val req = Request.Builder()
      .url("https://api.github.com/users/${ctx.args[0]}")
      .build()

    val res = D4rkBot.okHttpClient.newCall(req).await()

    if (res.code != 200) {
      ctx.channel.sendMessage(ctx.t("commands.github.profileNotFound")).queue()
      return
    }

    val user = DataObject.fromJson(withContext(Dispatchers.IO) { res.body!!.string() })
    res.close()

    val embed = EmbedBuilder {
      title = ctx.t("commands.github.title", listOf(user.getString("login")))
      url = "https://github.com/${user.getString("login")}"
      if (!user.isNull("bio")) description = "```\n${user.getString("bio")}```"
      field {
        name = ctx.t("commands.github.name")
        value = if (!user.isNull("name")) user.getString("name") else user.getString("login")
      }
      field {
        name = ctx.t("commands.github.id")
        value = user.getString("id")
      }
      field {
        name = ctx.t("commands.github.publicRepos")
        value = user.getString("public_repos")
      }
      field {
        name = ctx.t("commands.github.followers")
        value = user.getString("followers")
      }
      field {
        name = ctx.t("commands.github.following")
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
        name = ctx.t("commands.github.company")
        value = user.getString("company")
      }
    }

    if (!user.isNull("twitter_username")) {
      embed.field {
        name = "<:twitter:785165170547753002> Twitter"
        value =
          "[@${user.getString("twitter_username")}](https://twitter.com/${user.getString("twitter_username")})"
      }
    }

    if (!user.isNull("location")) {
      embed.field {
        name = ctx.t("commands.github.location")
        value = user.getString("location")
      }
    }

    embed.field {
      name = ctx.t("commands.github.created")
      value = "<t:${Instant.parse(user.getString("created_at")).epochSecond}:d> (<t:${
        Instant.parse(user.getString("created_at")).epochSecond
      }:R>)"
    }

    embed.field {
      name = ctx.t("commands.github.updated")
      value = "<t:${Instant.parse(user.getString("updated_at")).epochSecond}:d> (<t:${
        Instant.parse(user.getString("updated_at")).epochSecond
      }:R>)"
    }

    ctx.channel.sendMessageEmbeds(embed.build()).queue()
  }
}