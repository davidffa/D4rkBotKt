package com.github.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.messages.Embed
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.Region.*
import net.dv8tion.jda.api.entities.channel.ChannelType.*
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel
import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.time.Instant

class Channelinfo : Command(
  "channelinfo",
  listOf("chinfo"),
  "Info",
  listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_EXT_EMOJI),
  cooldown = 5
) {
  override suspend fun run(ctx: CommandContext) {
    val channel = if (ctx.args.isNotEmpty()) {
      Utils.findChannel(ctx.args.joinToString(" "), ctx.guild)
    } else ctx.channel

    if (channel == null) {
      ctx.channel.sendMessage(ctx.t("errors.channels.notfound")).queue()
      return
    }

    val channelType = when (channel.type) {
      TEXT -> ctx.t("commands.channelinfo.channeltypes.text")
      NEWS -> ctx.t("commands.channelinfo.channeltypes.news")
      VOICE -> ctx.t("commands.channelinfo.channeltypes.voice")
      CATEGORY -> ctx.t("commands.channelinfo.channeltypes.category")
      STAGE -> ctx.t("commands.channelinfo.channeltypes.stage")
      else -> ctx.t("commands.channelinfo.channeltypes.unknown")
    }

    val embed = Embed {
      title = ctx.t("commands.channelinfo.title", listOf(channel.name))
      color = Utils.randColor()

      field {
        name = ":id: ID"
        value = "`${channel.id}`"
      }
      field {
        name = ctx.t("commands.channelinfo.fields.type.name")
        value = "`$channelType`"
      }
      field {
        name = ctx.t("utils.created.name")
        value = ctx.t("utils.created.value", listOf(channel.timeCreated.toEpochSecond().toString()))
      }
      field {
        name = ctx.t("commands.channelinfo.fields.position.name")
        value = "`${(channel as IPositionableChannel).position}`"
      }
      if (channel is TextChannel) {
        field {
          name = ":underage: NSFW"
          value = "`${if (channel.isNSFW) ctx.t("global.yes") else ctx.t("global.no")}`"
        }
      }
      if (channel.type != CATEGORY) {
        field {
          name = ctx.t("commands.channelinfo.fields.category.name")
          value =
            "`${(channel as ICategorizableChannel).parentCategory?.name ?: ctx.t("commands.channelinfo.fields.category.none")}`"
        }
      } else {
        field {
          name = "<:chat:804050576647913522> Quantidade de canais na categoria"
          value = "`${ctx.guild.channels.filter { it is ICategorizableChannel && it.parentCategory == channel }.size}`"
        }
      }

      when (channel) {
        is VoiceChannel -> {
          field {
            name = ctx.t("commands.channelinfo.fields.bitrate.name")
            value = "`${channel.bitrate} Kbps`"
          }
          field {
            name = ctx.t("commands.channelinfo.fields.region.name")
            value = convertRegion(channel.region)
          }
          field {
            name = ctx.t("commands.channelinfo.fields.members.name")
            value = "`${if (channel.userLimit == 0) ctx.t("global.none") else channel.userLimit}`"
          }
        }
        is StageChannel -> {
          field {
            name = ctx.t("commands.channelinfo.fields.bitrate.name")
            value = "`${channel.bitrate} Kbps`"
          }
          field {
            name = ctx.t("commands.channelinfo.fields.region.name")
            value = convertRegion(channel.region)
          }
        }
        is TextChannel -> {
          field {
            name = ctx.t("commands.channelinfo.fields.topic.name")
            value = "```\n${channel.topic ?: ctx.t("global.none")}```"
            inline = false
          }
        }
      }

      timestamp = Instant.now()
      footer {
        name = ctx.author.asTag
        iconUrl = ctx.author.effectiveAvatarUrl
      }
    }

    ctx.channel.sendMessageEmbeds(embed).queue()
  }

  private fun convertRegion(region: Region): String {
    return when (region) {
      BRAZIL -> ":flag_br:"
      HONG_KONG -> ":flag_hk:"
      JAPAN -> ":flag_jp:"
      SOUTH_KOREA -> ":flag_kr:"
      RUSSIA -> ":flag_ru:"
      INDIA -> ":flag_in:"
      MILAN -> ":flag_it:"
      ROTTERDAM -> ":flag_nl:"
      SINGAPORE -> ":flag_sg:"
      SOUTH_AFRICA -> ":flag_sa:"
      SYDNEY -> ":flag_au:"
      US_CENTRAL -> ":flag_us:"
      US_EAST -> ":flag_us:"
      US_SOUTH -> ":flag_us:"
      US_WEST -> ":flag_us:"

      VIP_BRAZIL -> "VIP :flag_br:"
      VIP_HONG_KONG -> "VIP :flag_hk:"
      VIP_INDIA -> "VIP :flag_in:"
      VIP_JAPAN -> "VIP :flag_jp:"
      VIP_MILAN -> "VIP :flag_it:"
      VIP_ROTTERDAM -> "VIP :flag_nl:"
      VIP_RUSSIA -> "VIP :flag_ru:"
      VIP_SOUTH_KOREA -> "VIP :flag_kr:"
      VIP_SINGAPORE -> "VIP :flag_sg:"
      VIP_SOUTH_AFRICA -> "VIP :flag_sa:"
      VIP_SYDNEY -> "VIP :flag_au:"
      VIP_US_CENTRAL -> "VIP :flag_us:"
      VIP_US_EAST -> "VIP :flag_us:"
      VIP_US_SOUTH -> "VIP :flag_us:"
      VIP_US_WEST -> "VIP :flag_us:"
      Region.UNKNOWN -> ":question:"
      AUTOMATIC -> "`Auto`"
    }
  }
}