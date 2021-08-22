package me.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.Embed
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.Region.*
import net.dv8tion.jda.api.entities.ChannelType.*
import net.dv8tion.jda.api.entities.StageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import java.time.Instant

class Channelinfo : Command(
  "channelinfo",
  "Mostra informações sobre um canal do servidor.",
  listOf("chinfo"),
  "[ID do canal/Nome]",
  "Info",
  listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_EXT_EMOJI),
  cooldown = 5
) {
  override suspend fun run(ctx: CommandContext) {
    val channel = if (ctx.args.isNotEmpty()) {
      Utils.findChannel(ctx.args.joinToString(" "), ctx.guild)
    }else ctx.channel

    if (channel == null) {
      ctx.channel.sendMessage(ctx.t("errors.channels.notfound")).queue()
      return
    }

    val channelType = when (channel.type) {
      TEXT -> {
        if (channel is TextChannel) {
          if (channel.isNews) ctx.t("commands.channelinfo.channeltypes.news")
          else ctx.t("commands.channelinfo.channeltypes.text")
        }else ctx.t("commands.channelinfo.channeltypes.unknown")
      }
      VOICE -> ctx.t("commands.channelinfo.channeltypes.voice")
      CATEGORY -> ctx.t("commands.channelinfo.channeltypes.category")
      STORE -> ctx.t("commands.channelinfo.channeltypes.store")
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
        value = "`${channel.position}`"
      }
      if (channel.type != CATEGORY) {
        field {
          name = ctx.t("commands.channelinfo.fields.category.name")
          value = "`${if (channel.parent != null) channel.parent!!.name else ctx.t("commands.channelinfo.fields.category.none")}`"
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
          field {
            name = ctx.t("commands.channelinfo.fields.members.name")
            value = "`${if (channel.userLimit == 0) ctx.t("global.none") else channel.userLimit}`"
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
      AMSTERDAM -> ":flag_nl:"
      BRAZIL -> ":flag_br:"
      EUROPE -> ":flag_eu:"
      EU_CENTRAL -> ":flag_eu:"
      EU_WEST -> ":flag_eu:"
      FRANKFURT -> ":flag_de:"
      HONG_KONG -> ":flag_hk:"
      JAPAN -> ":flag_jp:"
      SOUTH_KOREA -> ":flag_kr:"
      LONDON -> ":flag_gb:"
      RUSSIA -> ":flag_ru:"
      INDIA -> ":flag_in:"
      SINGAPORE -> ":flag_sg:"
      SOUTH_AFRICA -> ":flag_sa:"
      SYDNEY -> ":flag_au:"
      US_CENTRAL -> ":flag_us:"
      US_EAST -> ":flag_us:"
      US_SOUTH -> ":flag_us:"
      US_WEST -> ":flag_us:"

      VIP_AMSTERDAM -> "VIP :flag_nl:"
      VIP_BRAZIL -> "VIP :flag_br:"
      VIP_EU_CENTRAL -> "VIP :flag_eu:"
      VIP_EU_WEST -> "VIP :flag_eu:"
      VIP_FRANKFURT -> "VIP :flag_de:"
      VIP_JAPAN -> "VIP :flag_jp:"
      VIP_SOUTH_KOREA -> "VIP :flag_kr:"
      VIP_LONDON -> "VIP :flag_gb:"
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