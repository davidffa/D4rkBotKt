package me.davidffa.d4rkbotkt.commands.info

import dev.minn.jda.ktx.EmbedBuilder
import dev.minn.jda.ktx.interactions.sendPaginator
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.OnlineStatus.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel.ALL
import net.dv8tion.jda.api.entities.Guild.ExplicitContentLevel.NO_ROLE
import net.dv8tion.jda.api.entities.Guild.VerificationLevel.*
import java.time.Instant

class Serverinfo : Command(
  "serverinfo",
  "Informações sobre o servidor.",
  listOf("si", "svinfo"),
  botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
  category = "Info",
  cooldown = 5
) {
  override suspend fun run(ctx: CommandContext) {
    var online = 0;
    var idle = 0;
    var dnd = 0;
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
      title = ":information_source: Informações do servidor ${ctx.guild.name}"
      field {
        name = ":id: ID"
        value = ctx.guild.id
      }
      field {
        name = ":crown: Dono do servidor"
        value = "${ctx.guild.owner?.asMention}"
      }
      field {
        name = ":underage: Filtro NSFW"
        value = when (ctx.guild.explicitContentLevel) {
          NO_ROLE -> "Sem cargo"
          ALL -> "Tudo"
          else -> "Desativado"
        }
      }
      field {
        name = ":zzz: Canal AFK"
        value = if (ctx.guild.afkChannel != null) ctx.guild.afkChannel!!.asMention else "Nenhum"
      }
      field {
        name = ":bookmark_tabs: Canal de regras"
        value = if (ctx.guild.rulesChannel != null) ctx.guild.rulesChannel!!.asMention else "Nenhum"
      }
      field {
        name = ":police_officer: Nível de verificação"
        value = when (ctx.guild.verificationLevel) {
          LOW -> "Baixo"
          MEDIUM -> "Médio"
          HIGH -> "Alto"
          VERY_HIGH -> "Muito Alto"
          else -> "Nenhum"
        }
      }
      field {
        name = ":date: Criado em"
        value =
          "<t:${ctx.guild.timeCreated.toInstant().epochSecond}:d> (<t:${ctx.guild.timeCreated.toInstant().epochSecond}:R>)"
      }
      field {
        name = ":calendar: Entrei em"
        value =
          "<t:${ctx.guild.selfMember.timeJoined.toInstant().epochSecond}:d> (<t:${ctx.guild.selfMember.timeJoined.toInstant().epochSecond}:R>)"
      }
      field {
        name = "<:badgebooster:803666384373809233> Boosts"
        value = "Nível: ${getBoostTier(ctx.guild.boostTier)}\n" +
                "Quantidade: ${ctx.guild.boostCount}"
      }
      field {
        name = ":grinning: Emojis [${ctx.guild.emotes.size}]"
        value = "Estáticos: ${ctx.guild.emotes.filter { !it.isAnimated }.size}\n" +
                "Animados: ${ctx.guild.emotes.filter { it.isAnimated }.size}"
      }
      field {
        name = ":busts_in_silhouette: Membros [${ctx.guild.memberCount}]"
        value = "<:online:804049640437448714> Online: $online\n" +
                "<:idle:804049737383673899> Ausente: $idle\n" +
                "<:dnd:804049759328403486> Ocupado: $dnd\n" +
                "<:offline:804049815713480715> Offline: $offline\n" +
                "<:bot:804028762307821578> Bots: ${ctx.guild.members.filter { it.user.isBot }.size}"
      }
      field {
        name = ":white_small_square: Canais [${ctx.guild.channels.size}]"
        value = "<:chat:804050576647913522> Texto: ${ctx.guild.textChannels.filter { !it.isNews }.size}\n" +
                ":microphone2: Voz: ${ctx.guild.voiceChannels.size}\n" +
                "<:stage:828651062184378389> Palco: ${ctx.guild.stageChannels.size}\n" +
                ":loudspeaker: Anúncios: ${ctx.guild.textChannels.filter { it.isNews }.size}\n" +
                ":shopping_bags: Loja: ${ctx.guild.storeChannels.size}\n" +
                ":diamond_shape_with_a_dot_inside: Categorias: ${ctx.guild.categories.size}"
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
    embed.description = "**Cargos [${ctx.guild.roles.size}]**\n${ctx.guild.roles.joinToString(" ") { it.asMention }}"

    val page2 = embed.build()

    ctx.channel.sendPaginator(page1, page2, expireAfter = 3 * 60 * 1000L, filter = {
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