package com.github.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.interactions.components.sendPaginator
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.OnlineStatus.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel.ALL
import net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel.NO_ROLE
import net.dv8tion.jda.api.entities.Guild.VerificationLevel.*
import java.time.Instant
import kotlin.math.min
import kotlin.time.Duration.Companion.minutes

class Serverinfo : Command(
  "serverinfo",
  listOf("si", "svinfo"),
  botPermissions = listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS),
  category = "Info",
  cooldown = 5
) {
  override suspend fun run(ctx: CommandContext) {
    var online = 0
    var idle = 0
    var dnd = 0
    var offline = 0

    ctx.guild.members.forEach {
      when (it.onlineStatus) {
        ONLINE -> ++online
        IDLE -> ++idle
        DO_NOT_DISTURB -> ++dnd
        else -> ++offline
      }
    }

    val embed = EmbedBuilder {
      title = ctx.t("commands.serverinfo.title", listOf(ctx.guild.name))
      field {
        name = ":id: ID"
        value = ctx.guild.id
      }
      field {
        name = ctx.t("commands.serverinfo.owner")
        value = "${ctx.guild.owner?.asMention}"
      }
      field {
        name = ctx.t("commands.serverinfo.nsfw.name")
        value = when (ctx.guild.explicitContentLevel) {
          NO_ROLE -> ctx.t("commands.serverinfo.nsfw.norole")
          ALL -> ctx.t("commands.serverinfo.nsfw.all")
          else -> ctx.t("commands.serverinfo.nsfw.disabled")
        }
      }
      field {
        name = ctx.t("commands.serverinfo.afk")
        value = if (ctx.guild.afkChannel != null) ctx.guild.afkChannel!!.asMention else ctx.t("global.none")
      }
      field {
        name = ctx.t("commands.serverinfo.rules")
        value = if (ctx.guild.rulesChannel != null) ctx.guild.rulesChannel!!.asMention else ctx.t("global.none")
      }
      field {
        name = ctx.t("commands.serverinfo.verificationlevel.name")
        value = when (ctx.guild.verificationLevel) {
          LOW -> ctx.t("commands.serverinfo.verificationlevel.low")
          MEDIUM -> ctx.t("commands.serverinfo.verificationlevel.medium")
          HIGH -> ctx.t("commands.serverinfo.verificationlevel.high")
          VERY_HIGH -> ctx.t("commands.serverinfo.verificationlevel.veryhigh")
          else -> ctx.t("global.none")
        }
      }
      field {
        name = ctx.t("utils.created.name")
        value = ctx.t("utils.created.value", listOf(ctx.guild.timeCreated.toEpochSecond().toString()))
      }
      field {
        name = ctx.t("commands.serverinfo.botjoined.name")
        value = ctx.t("utils.created.value", listOf(ctx.selfMember.timeJoined.toEpochSecond().toString()))
      }
      field {
        name = "<:badgebooster:803666384373809233> Boosts"
        value = "${ctx.t("commands.serverinfo.boosts.level")} ${getBoostTier(ctx.guild.boostTier)}\n" +
                "${ctx.t("commands.serverinfo.boosts.amount")} ${ctx.guild.boostCount}"
      }
      field {
        name = ":grinning: Emojis [${ctx.guild.emojis.size}]"
        value = "${ctx.t("commands.serverinfo.emojis.static")} ${ctx.guild.emojis.filter { !it.isAnimated }.size}\n" +
                "${ctx.t("commands.serverinfo.emojis.animated")} ${ctx.guild.emojis.filter { it.isAnimated }.size}"
      }
      field {
        name = ":busts_in_silhouette: ${ctx.t("commands.serverinfo.members.name")} [${ctx.guild.memberCount}]"
        value = "<:online:804049640437448714> Online: $online\n" +
                "<:idle:804049737383673899> ${ctx.t("commands.serverinfo.members.idle")} $idle\n" +
                "<:dnd:804049759328403486> ${ctx.t("commands.serverinfo.members.dnd")} $dnd\n" +
                "<:offline:804049815713480715> Offline: $offline\n" +
                "<:bot:804028762307821578> Bots: ${ctx.guild.members.filter { it.user.isBot }.size}"
      }
      field {
        name = ":white_small_square: ${ctx.t("commands.serverinfo.channels.name")} [${ctx.guild.channels.size}]"
        value =
          "<:chat:804050576647913522> ${ctx.t("commands.serverinfo.channels.text")} ${ctx.guild.textChannels.size}\n" +
                  ":microphone2: ${ctx.t("commands.serverinfo.channels.voice")} ${ctx.guild.voiceChannels.size}\n" +
                  "<:stage:828651062184378389> ${ctx.t("commands.serverinfo.channels.stage")} ${ctx.guild.stageChannels.size}\n" +
                  ":loudspeaker: ${ctx.t("commands.serverinfo.channels.news")} ${ctx.guild.newsChannels.size}\n" +
                  ":diamond_shape_with_a_dot_inside: ${ctx.t("commands.serverinfo.channels.categories")} ${ctx.guild.categories.size}"
      }
      color = Utils.randColor()
      footer {
        name = ctx.author.asTag
        iconUrl = ctx.author.effectiveAvatarUrl
      }
      timestamp = Instant.now()
    }

    if (ctx.guild.iconUrl != null) embed.thumbnail = "${ctx.guild.iconUrl}?size=4096"
    if (ctx.guild.bannerUrl != null) embed.image = "${ctx.guild.bannerUrl}?size=4096"

    val page1 = embed.build()

    embed.builder.clearFields()
    embed.description = "**${ctx.t("commands.serverinfo.roles")} [${ctx.guild.roles.size}]**\n${
      ctx.guild.roles.slice(
        0 until min(
          ctx.guild.roles.size,
          70
        )
      ).joinToString(" ") { it.asMention }
    }" +
            if (ctx.guild.roles.size > 70) "... (${
              ctx.t(
                "commands.serverinfo.more",
                listOf((ctx.guild.roles.size - 70).toString())
              )
            })" else ""

    val page2 = embed.build()

    ctx.channel.sendPaginator(page1, page2, expireAfter = 3.minutes, filter = {
      if (it.user.idLong == ctx.author.idLong) return@sendPaginator true
      return@sendPaginator false
    }).queue()
  }

  private fun getBoostTier(tier: Guild.BoostTier): String {
    return when (tier) {
      Guild.BoostTier.TIER_1 -> "1"
      Guild.BoostTier.TIER_2 -> "2"
      Guild.BoostTier.TIER_3 -> "3"
      else -> "0"
    }
  }
}