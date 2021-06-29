package me.davidffa.d4rkbotkt.utils

import com.mongodb.client.model.Updates
import dev.minn.jda.ktx.await
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.lavaplayer.PlayerManager
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import java.net.MalformedURLException
import java.net.URL
import java.time.OffsetDateTime
import java.util.*
import kotlin.math.min

object Utils {
    fun randColor(): Int {
        val rnd = Random()

        return rnd.nextInt(0xffffff + 1)
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

        return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    }

    fun formatDate(date: OffsetDateTime): String {
        return "${date.dayOfMonth}/${date.monthValue}/${date.year}"
    }

    suspend fun findUser(query: String, guild: Guild): User? {
        val jda = guild.jda
        var user: User? = null
        var id: String? = null

        if (Regex("^<@!?[0-9]{17,19}>$").matches(query)) id = query.replace(Regex("[<@!>]"), "")
        else if (Regex("^[0-9]+$").matches(query)) id = query

        if (id != null) {
            return try {
                jda.retrieveUserById(query).await()
            } catch (e: Exception) {
                null
            }
        }

        if (Regex("^#?[0-9]{4}$").matches(query)) {
            val u = guild.members.find { it.user.discriminator == query.replace("#", "") }?.user
            if (u != null) return u
        }

        var startsWith = false
        val lcQuery = query.lowercase()

        for (m in guild.members) {
            val name = m.effectiveName

            if (name == query || name.lowercase() == lcQuery) {
                user = m.user
                break
            }

            if (name.startsWith(query) || name.lowercase().startsWith(lcQuery)) {
                startsWith = true
                user = m.user
                continue
            }

            if (!startsWith && (name.contains(query) || name.lowercase().contains(lcQuery))) user = m.user
        }
        return user
    }

    fun hasPermissions(selfMember: Member, channel: TextChannel, permissions: List<Permission>): Boolean {
        return selfMember.getPermissions(channel).containsAll(permissions)
    }

    suspend fun canPlay(self: Member, member: Member, channel: TextChannel): Boolean {
        val memberVoiceState = member.voiceState

        if (!memberVoiceState!!.inVoiceChannel()) {
            channel.sendMessage(":x: Precisas de estar num canal de voz para executar esse comando!").queue()
            return false
        }

        val memberVoiceChannel = memberVoiceState.channel
        val selfPermissions = self.getPermissions(memberVoiceChannel!!)

        if (!selfPermissions.contains(Permission.VIEW_CHANNEL)) {
            channel.sendMessage(":x: Não tenho permissão para ver o teu canal de voz!").queue()
            return false
        }

        if (!selfPermissions.contains(Permission.VOICE_CONNECT)) {
            channel.sendMessage(":x: Não tenho permissão para entrar no teu canal de voz!").queue()
            return false
        }

        if (!selfPermissions.contains(Permission.VOICE_SPEAK)) {
            channel.sendMessage(":x: Não tenho permissão para falar no teu canal de voz!").queue()
            return false
        }

        val djRoleID = D4rkBot.guildCache[member.guild.idLong]!!.djRole

        if (djRoleID != null) {
            val djRole = member.guild.roleCache.getElementById(djRoleID)

            if (djRole == null) {
                D4rkBot.guildCache[member.guild.idLong]!!.djRole = null
                Database.guildDB.updateOneById(member.guild.id, Updates.set("djRole", null))
            }else {
                if (!member.roles.contains(djRole)) {
                    channel.sendMessage(":x: Precisas de estar no meu canal de voz para usar este comando!").queue()
                    return false
                }
            }
        }
        return true
    }

    suspend fun canUsePlayer(self: Member, member: Member, channel: TextChannel, forOwnTrack: Boolean = false, forAllQueueTracks: Boolean = false): Boolean {
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

        val djRoleID = D4rkBot.guildCache[member.guild.idLong]!!.djRole ?: return true

        val djRole = member.guild.roleCache.getElementById(djRoleID)

        if (djRole == null) {
            D4rkBot.guildCache[member.guild.idLong]!!.djRole = null
            Database.guildDB.updateOneById(member.guild.id, Updates.set("djRole", null))
            return true
        }

        if (member.roles.contains(djRole)) return true

        if (forOwnTrack && player.scheduler.current.requester.idLong == member.idLong) return true

        if (forAllQueueTracks) {
            if (player.scheduler.queue.find { it.requester.idLong != member.idLong } != null) {
                return false
            }
            return true
        }
        return false
    }

    fun isUrl(url: String): Boolean {
        return try {
            URL(url)
            true
        }catch (e: MalformedURLException) {
            false
        }
    }

    fun levenshteinDistance(lhs : CharSequence, rhs : CharSequence) : Int {
        if(lhs == rhs) { return 0 }
        if(lhs.isEmpty()) { return rhs.length }
        if(rhs.isEmpty()) { return lhs.length }

        val lhsLength = lhs.length + 1
        val rhsLength = rhs.length + 1

        var cost = Array(lhsLength) { it }
        var newCost = Array(lhsLength) { 0 }

        for (i in 1 until rhsLength) {
            newCost[0] = i

            for (j in 1 until lhsLength) {
                val match = if(lhs[j - 1] == rhs[i - 1]) 0 else 1

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
            Permission.CREATE_INSTANT_INVITE -> "Criar convites"
            Permission.KICK_MEMBERS -> "Expulsar Membros"
            Permission.BAN_MEMBERS -> "Banir Membros"
            Permission.ADMINISTRATOR -> "Administrador"
            Permission.MANAGE_CHANNEL -> "Gerenciar Canal"
            Permission.MANAGE_SERVER -> "Gerenciar Servidor"
            Permission.VIEW_AUDIT_LOGS -> "Ver o registo de auditoria"
            Permission.PRIORITY_SPEAKER -> "Voz Prioritária"
            Permission.VOICE_STREAM -> "Transmitir"
            Permission.VIEW_CHANNEL -> "Ver Canal"
            Permission.MESSAGE_WRITE -> "Enviar Mensagens"
            Permission.MESSAGE_TTS -> "Enviar mensagens em TTS"
            Permission.MESSAGE_MANAGE -> "Gerenciar Mensagens"
            Permission.MESSAGE_EMBED_LINKS -> "Inserir Links"
            Permission.MESSAGE_ATTACH_FILES -> "Anexar Arquivos"
            Permission.MESSAGE_READ -> "Ler mensagens"
            Permission.MESSAGE_HISTORY -> "Ler o histórico de mensagens"
            Permission.MANAGE_PERMISSIONS -> "Gerenciar Permissões"
            Permission.MESSAGE_MENTION_EVERYONE -> "Mencionar everyone"
            Permission.MESSAGE_EXT_EMOJI -> "Utilizar emojis externos"
            Permission.VIEW_GUILD_INSIGHTS -> "Ver análises do servidor"
            Permission.VOICE_CONNECT -> "Conectar ao canal de voz"
            Permission.VOICE_SPEAK -> "Falar no canal de voz"
            Permission.VOICE_MUTE_OTHERS -> "Silenciar membros"
            Permission.VOICE_DEAF_OTHERS -> "Ensurdecer membros"
            Permission.VOICE_MOVE_OTHERS -> "Mover membros"
            Permission.VOICE_USE_VAD -> "Usar deteção de voz"
            Permission.NICKNAME_CHANGE -> "Mudar de nickname"
            Permission.NICKNAME_MANAGE -> "Gerenciar nicknames"
            Permission.MANAGE_WEBHOOKS -> "Gerenciar Webhooks"
            Permission.MANAGE_EMOTES -> "Gerenciar Emojis"
            Permission.USE_SLASH_COMMANDS -> "Usar commandos de /"
            Permission.MESSAGE_ADD_REACTION -> "Adicionar reações"
            Permission.MANAGE_ROLES -> "Gerenciar Cargos"
            Permission.REQUEST_TO_SPEAK -> "Requisitar para falar"
            Permission.UNKNOWN -> "Desconhecido"
        }
        /*
            MANAGE_THREADS *	0x0400000000 (1 << 34)	Allows for deleting and archiving threads, and viewing all private threads	T
            USE_PUBLIC_THREADS	0x0800000000 (1 << 35)	Allows for creating and participating in threads	T
            USE_PRIVATE_THREADS
        */
    }
}