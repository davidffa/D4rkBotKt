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
      ctx.channel.sendMessage(":x: Canal não encontrado!").queue()
      return
    }

    val channelType = when (channel.type) {
      TEXT -> {
        if (channel is TextChannel) {
          if (channel.isNews) "Anúncios"
          else "Texto"
        }else "Desconhecido"
      }
      VOICE -> "Voz"
      CATEGORY -> "Categoria"
      STORE -> "Loja"
      STAGE -> "Palco"
      else -> "Desconhecido"
    }

    val embed = Embed {
      title = "Informações do canal ${channel.name}"
      color = Utils.randColor()

      field {
        name = ":id: ID"
        value = "`${channel.id}`"
      }
      field {
        name = ":diamond_shape_with_a_dot_inside: Tipo"
        value = "`$channelType`"
      }
      field {
        name = ":calendar: Criado em"
        value = "<t:${channel.timeCreated.toEpochSecond()}:d> (<t:${channel.timeCreated.toEpochSecond()}:R>)"
      }
      field {
        name = ":trophy: Posição"
        value = "`${channel.position}`"
      }
      if (channel.type != CATEGORY) {
        field {
          name = ":flag_white: Categoria"
          value = "`${if (channel.parent != null) channel.parent!!.name else "Nenhuma"}`"
        }
      }else {
        field {
          name = "<:chat:804050576647913522> Quantidade de canais na categoria"
          value = "`${ctx.guild.channels.filter { it.parent == channel }.size }`"
        }
      }

      when (channel) {
        is VoiceChannel -> {
          field {
            name = ":notes: Taxa de bits"
            value = "`${channel.bitrate} Kbps`"
          }
          field {
            name = ":map: Região"
            value = convertRegion(channel.region)
          }
          field {
            name = ":busts_in_silhouette: Limite de membros"
            value = "`${if (channel.userLimit == 0) "Nenhum" else channel.userLimit}`"
          }
        }
        is StageChannel -> {
          field {
            name = ":notes: Taxa de bits"
            value = "`${channel.bitrate} Kbps`"
          }
          field {
            name = ":map: Região"
            value = convertRegion(channel.region)
          }
          field {
            name = ":busts_in_silhouette: Limite de membros"
            value = "`${if (channel.userLimit == 0) "Nenhum" else channel.userLimit}`"
          }
        }
        is TextChannel -> {
          field {
            name = ":question: Tópico"
            value = "```\n${channel.topic ?: "Nenhum"}```"
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
      Region.UNKNOWN -> "`Desconhecida`"
      AUTOMATIC -> "`Auto`"
    }
  }
}