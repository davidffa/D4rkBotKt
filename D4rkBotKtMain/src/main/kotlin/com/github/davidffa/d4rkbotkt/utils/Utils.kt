package com.github.davidffa.d4rkbotkt.utils

import com.mongodb.client.model.Updates
import dev.minn.jda.ktx.coroutines.await
import com.github.davidffa.d4rkbotkt.D4rkBot
import com.github.davidffa.d4rkbotkt.Database
import com.github.davidffa.d4rkbotkt.audio.PlayerManager
import com.github.davidffa.d4rkbotkt.audio.receive.ReceiverManager
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.Permission.*
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
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

    if (Regex("^<@!?\\d{17,19}>$").matches(query)) id = query.replace(Regex("[<@!>]"), "")
    else if (Regex("^\\d{17,19}$").matches(query)) id = query

    if (id != null) {
      return try {
        jda.retrieveUserById(id).await()
      } catch (e: Exception) {
        null
      }
    }

    if (Regex("^#?\\d{4}$").matches(query)) {
      val u = guild.members.find { it.user.discriminator == query.replace("#", "") }?.user
      if (u != null) return u
    }

    val lcQuery = query.lowercase()

    var startsWith = false

    for (m in guild.members) {
      val name = m.effectiveName

      if (name == query || name.lowercase() == lcQuery) {
        user = m.user
        break
      }

      if (name.startsWith(query) || name.lowercase().startsWith(lcQuery)) {
        user = m.user
        startsWith = true
        continue
      }

      if (!startsWith && (name.contains(query) || name.lowercase().contains(lcQuery))) user = m.user
    }
    return user
  }

  fun findRole(query: String, guild: Guild): Role? {
    val id = if (Regex("^<@&\\d{17,19}>$").matches(query)) query.replace(Regex("[<@&>]"), "")
    else if (Regex("^\\d{17,19}$").matches(query)) query
    else null

    if (id != null) {
      return guild.roleCache.getElementById(id)
    }

    var role: Role? = null
    val lcQuery = query.lowercase()

    var startsWith = false

    for (m in guild.roleCache) {
      val name = m.name

      if (name == query || name.lowercase() == lcQuery) {
        role = m
        break
      }

      if (name.startsWith(query) || name.lowercase().startsWith(lcQuery)) {
        role = m
        startsWith = true
        continue
      }

      if (!startsWith && (name.contains(query) || name.lowercase().contains(lcQuery))) role = m
    }
    return role
  }

  fun findChannel(query: String, guild: Guild): GuildChannel? {
    val id = if (Regex("^<&\\d{17,19}>$").matches(query)) query.replace(Regex("[<#>]"), "")
    else if (Regex("^\\d{17,19}$").matches(query)) query
    else null

    if (id != null) {
      return guild.getGuildChannelById(id)
    }

    var channel: GuildChannel? = null
    val lcQuery = query.lowercase()

    var startsWith = false

    for (m in guild.channels) {
      val name = m.name

      if (name == query || name.lowercase() == lcQuery) {
        channel = m
        break
      }

      if (name.startsWith(query) || name.lowercase().startsWith(lcQuery)) {
        channel = m
        startsWith = true
        continue
      }

      if (!startsWith && (name.contains(query) || name.lowercase().contains(lcQuery))) channel = m
    }
    return channel
  }

  fun hasPermissions(selfMember: Member, channel: GuildMessageChannel, permissions: List<Permission>): Boolean {
    return selfMember.getPermissions(channel).containsAll(permissions)
  }

  fun canRecord(t: (String) -> String, self: Member, member: Member, channel: GuildMessageChannel): Boolean {
    val memberVoiceState = member.voiceState
    val selfChannel = member.guild.audioManager.connectedChannel

    if (!memberVoiceState!!.inAudioChannel()) {
      channel.sendMessage(t("errors.notInVoiceChannel")).queue()
      return false
    }

    val memberVoiceChannel = memberVoiceState.channel
    val selfPermissions = self.getPermissions(memberVoiceChannel!!)

    if (!selfPermissions.contains(VIEW_CHANNEL)) {
      channel.sendMessage(t("errors.missingViewPerm")).queue()
      return false
    }

    if (!selfPermissions.contains(VOICE_CONNECT)) {
      channel.sendMessage(t("errors.missingConnectPerm")).queue()
      return false
    }

    if (selfChannel == null) {
      if (memberVoiceChannel.userLimit == (memberVoiceChannel as VoiceChannel).members.size && !selfPermissions.contains(
          MANAGE_CHANNEL
        )
      ) {
        channel.sendMessage(t("errors.fullVoiceChannel")).queue()
        return false
      }
    } else {
      if (selfChannel.idLong != memberVoiceChannel.idLong) {
        channel.sendMessage(":x: Precisas de estar no meu canal de voz para executar esse comando!").queue()
        return false
      }
    }

    if (PlayerManager.musicManagers.contains(self.guild.idLong)) {
      channel.sendMessage(t("errors.recordWhilePlaying")).queue()
      return false
    }

    return true
  }

  suspend fun canPlay(t: (String) -> String, self: Member, member: Member, channel: GuildMessageChannel): Boolean {
    val memberVoiceState = member.voiceState
    val selfChannel = member.guild.audioManager.connectedChannel

    if (!memberVoiceState!!.inAudioChannel()) {
      channel.sendMessage(t("errors.notInVoiceChannel")).queue()
      return false
    }

    val memberVoiceChannel = memberVoiceState.channel
    val selfPermissions = self.getPermissions(memberVoiceChannel!!)

    if (!selfPermissions.contains(VIEW_CHANNEL)) {
      channel.sendMessage(t("errors.missingViewPerm")).queue()
      return false
    }

    if (!selfPermissions.contains(VOICE_CONNECT)) {
      channel.sendMessage(t("errors.missingConnectPerm")).queue()
      return false
    }

    if (!selfPermissions.contains(VOICE_SPEAK)) {
      channel.sendMessage(t("errors.missingSpeakPerm")).queue()
      return false
    }

    if (selfChannel == null) {
      if (memberVoiceChannel.userLimit == (memberVoiceChannel as VoiceChannel).members.size && !selfPermissions.contains(
          MANAGE_CHANNEL
        )
      ) {
        channel.sendMessage(t("errors.fullVoiceChannel")).queue()
        return false
      }
      return true
    }

    if (ReceiverManager.receiveManagers.contains(self.guild.idLong)) {
      channel.sendMessage(t("errors.playWhileRecording")).queue()
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
          channel.sendMessage(t("errors.notInBotVC")).queue()
          return false
        }
      }
    }
    return true
  }

  suspend fun canUsePlayer(
    t: (String, List<String>?) -> String,
    self: Member,
    member: Member,
    channel: GuildMessageChannel,
    forOwnTrack: Boolean = false,
    forAllQueueTracks: Boolean = false,
    trackPosition: Int? = null
  ): Boolean {
    val memberVoiceState = member.voiceState
    val selfVoiceState = self.voiceState

    val player = PlayerManager.musicManagers[self.guild.idLong]

    if (player == null) {
      channel.sendMessage(t("errors.notplaying", null)).queue()
      return false
    }

    if (!memberVoiceState!!.inAudioChannel()) {
      channel.sendMessage(t("errors.notInVoiceChannel", null)).queue()
      return false
    }

    val memberVoiceChannel = memberVoiceState.channel

    if (selfVoiceState!!.inAudioChannel() && memberVoiceChannel != selfVoiceState.channel) {
      channel.sendMessage(t("errors.notInBotVC", null)).queue()
      return false
    }

    if (trackPosition != null) {
      if (player.scheduler.queue.isEmpty()) {
        channel.sendMessage(t("errors.emptyqueue", null)).queue()
        return false
      }

      val queue = player.scheduler.queue.toMutableList()
      val track = queue.getOrNull(trackPosition - 1)

      if (track == null) {
        channel.sendMessage(t("errors.noTrackAtPosition", null)).queue()
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

    if (selfVoiceState.channel!!.members.filter { !it.user.isBot }.size == 1) return true
    if (member.roles.contains(djRole)) return true
    if (forOwnTrack && (player.scheduler.current.requester.idLong == member.idLong || !selfVoiceState.channel!!.members.contains(
        player.scheduler.current.requester
      ))
    ) return true

    if (forAllQueueTracks) {
      if (player.scheduler.current.requester.idLong != member.idLong ||
        player.scheduler.queue.find { it.requester.idLong != member.idLong } != null) {
        channel.sendMessage(t("errors.allQueueRequested", null))
          .queue()
        return false
      }
      return true
    }

    if (forOwnTrack) {
      if (trackPosition != null) {
        channel.sendMessage(t("errors.onlyDJAndOther", listOf(djRole.name)))
          .queue()
      } else {
        channel.sendMessage(t("errors.onlyDJAndCurrent", listOf(djRole.name)))
          .queue()
      }
    } else {
      channel.sendMessage(t("errors.onlyDJ", listOf(djRole.name))).queue()
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

  fun translatePermissions(permissions: List<Permission>, t: (String) -> String): List<String> {
    return permissions.map {
      translatePermission(it, t)
    }
  }

  fun translatePermission(permission: Permission, t: (String) -> String): String {
    return when (permission) {
      CREATE_INSTANT_INVITE -> t("permissions.createInstantInvite")
      KICK_MEMBERS -> t("permissions.kickMembers")
      BAN_MEMBERS -> t("permissions.banMembers")
      ADMINISTRATOR -> t("permissions.administrator")
      MANAGE_CHANNEL -> t("permissions.manageChannel")
      MANAGE_SERVER -> t("permissions.manageServer")
      VIEW_AUDIT_LOGS -> t("permissions.viewAuditLogs")
      PRIORITY_SPEAKER -> t("permissions.prioritySpeaker")
      VOICE_STREAM -> t("permissions.voiceStream")
      VIEW_CHANNEL -> t("permissions.viewChannel")
      MESSAGE_SEND -> t("permissions.messageWrite")
      MESSAGE_TTS -> t("permissions.messageTTS")
      MESSAGE_MANAGE -> t("permissions.messageManage")
      MESSAGE_EMBED_LINKS -> t("permissions.messageEmbedLinks")
      MESSAGE_ATTACH_FILES -> t("permissions.messageAttachFiles")
      MESSAGE_HISTORY -> t("permissions.messageHistory")
      MANAGE_PERMISSIONS -> t("permissions.managePermissions")
      MESSAGE_MENTION_EVERYONE -> t("permissions.mentionEveryone")
      MESSAGE_EXT_EMOJI -> t("permissions.extEmoji")
      MESSAGE_EXT_STICKER -> t("permissions.extSticker")
      VIEW_GUILD_INSIGHTS -> t("permissions.viewGuildInsights")
      VOICE_CONNECT -> t("permissions.voiceConnect")
      VOICE_SPEAK -> t("permissions.voiceSpeak")
      VOICE_MUTE_OTHERS -> t("permissions.voiceMute")
      VOICE_DEAF_OTHERS -> t("permissions.voiceDeaf")
      VOICE_MOVE_OTHERS -> t("permissions.voiceMove")
      VOICE_USE_VAD -> t("permissions.voiceVad")
      NICKNAME_CHANGE -> t("permissions.nicknameChange")
      NICKNAME_MANAGE -> t("permissions.manageNicknames")
      MANAGE_WEBHOOKS -> t("permissions.manageWebhooks")
      MANAGE_EMOJIS_AND_STICKERS -> t("permissions.manageEmotes")
      USE_APPLICATION_COMMANDS -> t("permissions.useApplicationCommands")
      MESSAGE_ADD_REACTION -> t("permissions.addReactions")
      MANAGE_ROLES -> t("permissions.manageRoles")
      REQUEST_TO_SPEAK -> t("permissions.requestToSpeak")
      MANAGE_THREADS -> t("permissions.manageThreads")
      CREATE_PUBLIC_THREADS -> t("permissions.createPublicThreads")
      CREATE_PRIVATE_THREADS -> t("permissions.createPrivateThreads")
      MESSAGE_SEND_IN_THREADS -> t("permissions.messageSendInThreads")
      VOICE_START_ACTIVITIES -> t("permissions.voiceStartActivities")
      MODERATE_MEMBERS -> t("permissions.moderateMembers")
      MANAGE_EVENTS -> t("permissions.manageEvents")
      UNKNOWN -> t("permissions.unknown")
    }
  }
}