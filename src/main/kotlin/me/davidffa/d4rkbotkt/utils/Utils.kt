package me.davidffa.d4rkbotkt.utils

import com.mongodb.client.model.Updates
import dev.minn.jda.ktx.await
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.audio.receive.ReceiverManager
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.Permission.*
import net.dv8tion.jda.api.entities.*
import java.net.MalformedURLException
import java.net.URL
import kotlin.math.min

object Utils {
  fun randColor(): Int {
    return (0..0xffffff).random()
  }

  fun msToDate(time: Long): String {
    val t = time / 1000

    val s = t % 60
    val m = (t / 60) % 60
    val h = (t / 60 / 60) % 24
    val d = t / 60 / 60 / 24

    return "${d}D:${h}H:${m}M:${s}S"
  }

  fun msToHour(time: Long): String {
    val t = time / 1000

    val s = t % 60
    val m = (t / 60) % 60
    val h = (t / 60 / 60) % 24

    return if (h != 0L) {
      "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    } else {
      "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    }
  }

  suspend fun findUser(query: String, guild: Guild): User? {
    val jda = guild.jda
    var user: User? = null
    var id: String? = null

    if (Regex("^<@!?[0-9]{17,18}>$").matches(query)) id = query.replace(Regex("[<@!>]"), "")
    else if (Regex("^[0-9]{17,18}$").matches(query)) id = query

    if (id != null) {
      return try {
        jda.retrieveUserById(id).await()
      } catch (e: Exception) {
        null
      }
    }

    if (Regex("^#?[0-9]{4}$").matches(query)) {
      val u = guild.members.find { it.user.discriminator == query.replace("#", "") }?.user
      if (u != null) return u
    }

    val lcQuery = query.lowercase()

    for (m in guild.members) {
      val name = m.effectiveName

      if (name == query || name.lowercase() == lcQuery) {
        user = m.user
        break
      }

      if (name.startsWith(query) || name.lowercase().startsWith(lcQuery)) {
        user = m.user
        break
      }

      if (name.contains(query) || name.lowercase().contains(lcQuery)) user = m.user
    }
    return user
  }

  fun findRole(query: String, guild: Guild): Role? {
    val id = if (Regex("^<&\\d{17,18}>$").matches(query)) query.replace(Regex("[<&>]"), "")
    else if (Regex("^\\d{17,18}$").matches(query)) query
    else null

    if (id != null) {
      return guild.roleCache.getElementById(id)
    }

    var role: Role? = null
    val lcQuery = query.lowercase()

    for (m in guild.roleCache) {
      val name = m.name

      if (name == query || name.lowercase() == lcQuery) {
        role = m
        break
      }

      if (name.startsWith(query) || name.lowercase().startsWith(lcQuery)) {
        role = m
        break
      }

      if (name.contains(query) || name.lowercase().contains(lcQuery)) role = m
    }
    return role
  }

  fun findChannel(query: String, guild: Guild): GuildChannel? {
    val id = if (Regex("^<&\\d{17,18}>$").matches(query)) query.replace(Regex("[<#>]"), "")
    else if (Regex("^\\d{17,18}$").matches(query)) query
    else null

    if (id != null) {
      return guild.getGuildChannelById(id)
    }

    var channel: GuildChannel? = null
    val lcQuery = query.lowercase()

    for (m in guild.channels) {
      val name = m.name

      if (name == query || name.lowercase() == lcQuery) {
        channel = m
        break
      }

      if (name.startsWith(query) || name.lowercase().startsWith(lcQuery)) {
        channel = m
        break
      }

      if (name.contains(query) || name.lowercase().contains(lcQuery)) channel = m
    }
    return channel
  }

  fun hasPermissions(selfMember: Member, channel: TextChannel, permissions: List<Permission>): Boolean {
    return selfMember.getPermissions(channel).containsAll(permissions)
  }

  fun canRecord(self: Member, member: Member, channel: TextChannel): Boolean {
    val memberVoiceState = member.voiceState
    val selfChannel = member.guild.audioManager.connectedChannel

    if (!memberVoiceState!!.inVoiceChannel()) {
      channel.sendMessage(":x: Precisas de estar num canal de voz para executar esse comando!").queue()
      return false
    }

    val memberVoiceChannel = memberVoiceState.channel
    val selfPermissions = self.getPermissions(memberVoiceChannel!!)

    if (!selfPermissions.contains(VIEW_CHANNEL)) {
      channel.sendMessage(":x: Não tenho permissão para ver o teu canal de voz!").queue()
      return false
    }

    if (!selfPermissions.contains(VOICE_CONNECT)) {
      channel.sendMessage(":x: Não tenho permissão para entrar no teu canal de voz!").queue()
      return false
    }

    if (selfChannel == null) {
      if (memberVoiceChannel.userLimit == memberVoiceChannel.members.size && !selfPermissions.contains(MANAGE_CHANNEL)) {
        channel.sendMessage(":x: O teu canal de voz está cheio!").queue()
        return false
      }
    } else {
      if (selfChannel.idLong != memberVoiceChannel.idLong) {
        channel.sendMessage(":x: Precisas de estar no meu canal de voz para executar esse comando!").queue()
        return false
      }
    }

    if (PlayerManager.musicManagers.contains(self.guild.idLong)) {
      channel.sendMessage(":x: Não posso gravar áudio enquanto toco música!").queue()
      return false
    }

    return true
  }

  suspend fun canPlay(self: Member, member: Member, channel: TextChannel): Boolean {
    val memberVoiceState = member.voiceState
    val selfChannel = member.guild.audioManager.connectedChannel

    if (!memberVoiceState!!.inVoiceChannel()) {
      channel.sendMessage(":x: Precisas de estar num canal de voz para executar esse comando!").queue()
      return false
    }

    val memberVoiceChannel = memberVoiceState.channel
    val selfPermissions = self.getPermissions(memberVoiceChannel!!)

    if (!selfPermissions.contains(VIEW_CHANNEL)) {
      channel.sendMessage(":x: Não tenho permissão para ver o teu canal de voz!").queue()
      return false
    }

    if (!selfPermissions.contains(VOICE_CONNECT)) {
      channel.sendMessage(":x: Não tenho permissão para entrar no teu canal de voz!").queue()
      return false
    }

    if (!selfPermissions.contains(VOICE_SPEAK)) {
      channel.sendMessage(":x: Não tenho permissão para falar no teu canal de voz!").queue()
      return false
    }

    if (selfChannel == null) {
      if (memberVoiceChannel.userLimit == memberVoiceChannel.members.size && !selfPermissions.contains(MANAGE_CHANNEL)) {
        channel.sendMessage(":x: O teu canal de voz está cheio!").queue()
        return false
      }
      return true
    }

    if (ReceiverManager.receiveManagers.contains(self.guild.idLong)) {
      channel.sendMessage(":x: Não posso tocar música enquanto gravo áudio!").queue()
      return false
    }

    val djRoleID = D4rkBot.guildCache[member.guild.idLong]!!.djRole

    if (djRoleID != null) {
      val djRole = member.guild.roleCache.getElementById(djRoleID)

      if (djRole == null) {
        D4rkBot.guildCache[member.guild.idLong]!!.djRole = null
        Database.guildDB.updateOneById(member.guild.id, Updates.set("djRole", null))
      } else {
        if (!member.roles.contains(djRole)) {
          if (selfChannel.idLong == memberVoiceChannel.idLong) return true
          channel.sendMessage(":x: Precisas de estar no meu canal de voz para usar este comando!").queue()
          return false
        }
      }
    }
    return true
  }

  suspend fun canUsePlayer(
    self: Member,
    member: Member,
    channel: TextChannel,
    forOwnTrack: Boolean = false,
    forAllQueueTracks: Boolean = false,
    trackPosition: Int? = null
  ): Boolean {
    val memberVoiceState = member.voiceState
    val selfVoiceState = self.voiceState

    val player = PlayerManager.musicManagers[self.guild.idLong]

    if (player == null) {
      channel.sendMessage(":x: Não estou a tocar nada de momento!").queue()
      return false
    }

    if (!memberVoiceState!!.inVoiceChannel()) {
      channel.sendMessage(":x: Precisas de estar num canal de voz para executar esse comando!").queue()
      return false
    }

    val memberVoiceChannel = memberVoiceState.channel

    if (selfVoiceState!!.inVoiceChannel() && memberVoiceChannel != selfVoiceState.channel) {
      channel.sendMessage(":x: Precisas de estar no meu canal de voz para usar este comando!").queue()
      return false
    }

    if (trackPosition != null) {
      if (player.scheduler.queue.isEmpty()) {
        channel.sendMessage(":x: A queue está vazia!").queue()
        return false
      }

      val queue = player.scheduler.queue.toMutableList()
      val track = queue.getOrNull(trackPosition - 1)

      if (track == null) {
        channel.sendMessage(":x: Não há nenhuma música nessa posição da queue.").queue()
        return false
      }

      if (track.requester.idLong == member.idLong) return true
    }

    val djRoleID = D4rkBot.guildCache[member.guild.idLong]!!.djRole ?: return true
    val djRole = member.guild.roleCache.getElementById(djRoleID)

    if (djRole == null) {
      D4rkBot.guildCache[member.guild.idLong]!!.djRole = null
      Database.guildDB.updateOneById(member.guild.id, Updates.set("djRole", null))
      return true
    }

    if (selfVoiceState.channel!!.members.filter { !it.user.isBot }.size <= 2) return true
    if (member.roles.contains(djRole)) return true
    if (forOwnTrack && player.scheduler.current.requester.idLong == member.idLong) return true

    if (forAllQueueTracks) {
      if (player.scheduler.queue.find { it.requester.idLong != member.idLong } != null) {
        channel.sendMessage(":x: Todas as músicas da queue têm de ser requisitadas por ti para poderes usar esse comando!")
          .queue()
        return false
      }
      return true
    }

    if (forOwnTrack) {
      if (trackPosition != null) {
        channel.sendMessage(":x: Apenas alguém com o cargo de DJ (`${djRole.name}`) ou quem requisitou a música dessa posição a pode remover da queue.")
          .queue()
      } else {
        channel.sendMessage(":x: Apenas alguém com o cargo de DJ (`${djRole.name}`) ou quem requisitou esta música pode usar esse comando.")
          .queue()
      }
    } else {
      channel.sendMessage(":x: Precisas do cargo de DJ (`${djRole.name}`) para poderes usar esse comando.").queue()
    }
    return false
  }

  fun isUrl(url: String): Boolean {
    return try {
      URL(url)
      true
    } catch (e: MalformedURLException) {
      false
    }
  }

  fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
    if (lhs == rhs) {
      return 0
    }
    if (lhs.isEmpty()) {
      return rhs.length
    }
    if (rhs.isEmpty()) {
      return lhs.length
    }

    val lhsLength = lhs.length + 1
    val rhsLength = rhs.length + 1

    var cost = Array(lhsLength) { it }
    var newCost = Array(lhsLength) { 0 }

    for (i in 1 until rhsLength) {
      newCost[0] = i

      for (j in 1 until lhsLength) {
        val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

        val costReplace = cost[j - 1] + match
        val costInsert = cost[j] + 1
        val costDelete = newCost[j - 1] + 1

        newCost[j] = min(min(costInsert, costDelete), costReplace)
      }

      val swap = cost
      cost = newCost
      newCost = swap
    }

    return cost[lhsLength - 1]
  }

  fun translatePermissions(permissions: List<Permission>): List<String> {
    return permissions.map {
      translatePermission(it)
    }
  }

  fun translatePermission(permission: Permission): String {
    return when (permission) {
      CREATE_INSTANT_INVITE -> "Criar convites"
      KICK_MEMBERS -> "Expulsar Membros"
      BAN_MEMBERS -> "Banir Membros"
      ADMINISTRATOR -> "Administrador"
      MANAGE_CHANNEL -> "Gerenciar Canal"
      MANAGE_SERVER -> "Gerenciar Servidor"
      VIEW_AUDIT_LOGS -> "Ver o registo de auditoria"
      PRIORITY_SPEAKER -> "Voz Prioritária"
      VOICE_STREAM -> "Transmitir"
      VIEW_CHANNEL -> "Ver Canal"
      MESSAGE_WRITE -> "Enviar Mensagens"
      MESSAGE_TTS -> "Enviar mensagens em TTS"
      MESSAGE_MANAGE -> "Gerenciar Mensagens"
      MESSAGE_EMBED_LINKS -> "Inserir Links"
      MESSAGE_ATTACH_FILES -> "Anexar Arquivos"
      MESSAGE_READ -> "Ler mensagens"
      MESSAGE_HISTORY -> "Ler o histórico de mensagens"
      MANAGE_PERMISSIONS -> "Gerenciar Permissões"
      MESSAGE_MENTION_EVERYONE -> "Mencionar everyone"
      MESSAGE_EXT_EMOJI -> "Utilizar emojis externos"
      VIEW_GUILD_INSIGHTS -> "Ver análises do servidor"
      VOICE_CONNECT -> "Conectar ao canal de voz"
      VOICE_SPEAK -> "Falar no canal de voz"
      VOICE_MUTE_OTHERS -> "Silenciar membros"
      VOICE_DEAF_OTHERS -> "Ensurdecer membros"
      VOICE_MOVE_OTHERS -> "Mover membros"
      VOICE_USE_VAD -> "Usar deteção de voz"
      NICKNAME_CHANGE -> "Mudar de nickname"
      NICKNAME_MANAGE -> "Gerenciar nicknames"
      MANAGE_WEBHOOKS -> "Gerenciar Webhooks"
      MANAGE_EMOTES -> "Gerenciar Emojis"
      USE_SLASH_COMMANDS -> "Usar commandos de /"
      MESSAGE_ADD_REACTION -> "Adicionar reações"
      MANAGE_ROLES -> "Gerenciar Cargos"
      REQUEST_TO_SPEAK -> "Requisitar para falar"
      MANAGE_THREADS -> "Gerenciar Threads"
      USE_PUBLIC_THREADS -> "Usar Threads públicos"
      USE_PRIVATE_THREADS -> "Usar Threads privados"
      UNKNOWN -> "Desconhecido"
    }
  }
}