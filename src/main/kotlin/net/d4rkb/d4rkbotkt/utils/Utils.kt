package net.d4rkb.d4rkbotkt.utils

import net.d4rkb.d4rkbotkt.lavaplayer.PlayerManager
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import java.net.MalformedURLException
import java.net.URL
import java.time.OffsetDateTime
import java.util.*
import kotlin.collections.ArrayList
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

    fun findUser(query: String, guild: Guild): User? {
        val jda = guild.jda
        var user: User? = null

        if (Regex("^<@!?[0-9]{17,19}>$").matches(query)) {
            try {
                user = jda.retrieveUserById(query.replace(Regex("[<@!>]"), "")).complete()
            }catch (e: Exception) {}

            return user
        }

        if (Regex("^[0-9]+$").matches(query) && query.length >= 17 && query.length <= 19) {
            try {
                user = jda.retrieveUserById(query).complete()
            }catch (e: Exception) {}

            return user
        }

        if (Regex("^#?[0-9]{4}$").matches(query)) {
            user = guild.members.find { it.user.discriminator == query }?.user
        }

        if (user == null) {
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
        }

        return user
    }

    fun canPlay(self: Member, member: Member, channel: TextChannel): Boolean {
        val memberVoiceState = member.voiceState
        val selfVoiceState = self.voiceState

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

        if (selfVoiceState!!.inVoiceChannel() && memberVoiceChannel != selfVoiceState.channel) {
            channel.sendMessage(":x: Precisas de estar no meu canal de voz para usar este comando!").queue()
            return false
        }

        return true
    }

    fun canUsePlayer(self: Member, member: Member, channel: TextChannel): Boolean {
        val memberVoiceState = member.voiceState
        val selfVoiceState = self.voiceState

        if (!selfVoiceState!!.inVoiceChannel() || !PlayerManager.hasMusicManager(self.guild)) {
            channel.sendMessage(":x: Não estou a tocar nada de momento!").queue()
            return false
        }

        if (!memberVoiceState!!.inVoiceChannel()) {
            channel.sendMessage(":x: Precisas de estar num canal de voz para executar esse comando!").queue()
            return false
        }

        val memberVoiceChannel = memberVoiceState.channel

        //TODO DJ STUFF (cache :/)

        if (selfVoiceState.inVoiceChannel() && memberVoiceChannel != selfVoiceState.channel) {
            channel.sendMessage(":x: Precisas de estar no meu canal de voz para usar este comando!").queue()
            return false
        }
        return true
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

    fun translatePermissions(permissions: List<Permission>): ArrayList<String> {
        val translatedPerms = ArrayList<String>()

        permissions.forEach { permission ->
            translatedPerms.add(translatePermission(permission))
        }
        return translatedPerms
    }

    fun translatePermission(permission: Permission): String {
        when (permission) {
            Permission.CREATE_INSTANT_INVITE -> {
                return "Criar convites"
            }
            Permission.KICK_MEMBERS -> {
                return "Expulsar Membros"
            }
            Permission.BAN_MEMBERS -> {
                return "Banir Membros"
            }
            Permission.ADMINISTRATOR -> {
                return "Administrador"
            }
            Permission.MANAGE_CHANNEL -> {
                return "Gerenciar Canal"
            }
            Permission.MANAGE_SERVER -> {
                return "Gerenciar Servidor"
            }
            Permission.VIEW_AUDIT_LOGS -> {
                return "Ver o registo de auditoria"
            }
            Permission.PRIORITY_SPEAKER -> {
                return "Voz Prioritária"
            }
            Permission.VOICE_STREAM -> {
                return "Transmitir"
            }
            Permission.VIEW_CHANNEL -> {
                return "Ver Canal"
            }
            Permission.MESSAGE_WRITE -> {
                return "Enviar Mensagens"
            }
            Permission.MESSAGE_TTS -> {
                return "Enviar mensagens em TTS"
            }
            Permission.MESSAGE_MANAGE -> {
                return "Gerenciar Mensagens"
            }
            Permission.MESSAGE_EMBED_LINKS -> {
                return "Inserir Links"
            }
            Permission.MESSAGE_ATTACH_FILES -> {
                return "Anexar Arquivos"
            }
            Permission.MESSAGE_READ -> {
                return "Ler mensagens"
            }
            Permission.MESSAGE_HISTORY -> {
                return "Ler o histórico de mensagens"
            }
            Permission.MANAGE_PERMISSIONS -> {
                return "Gerenciar Permissões"
            }
            Permission.MESSAGE_MENTION_EVERYONE -> {
                return "Mencionar everyone"
            }
            Permission.MESSAGE_EXT_EMOJI -> {
                return "Utilizar emojis externos"
            }
            Permission.VIEW_GUILD_INSIGHTS -> {
                return "Ver análises do servidor"
            }
            Permission.VOICE_CONNECT -> {
                return "Conectar ao canal de voz"
            }
            Permission.VOICE_SPEAK -> {
                return "Falar no canal de voz"
            }
            Permission.VOICE_MUTE_OTHERS -> {
                return "Silenciar membros"
            }
            Permission.VOICE_DEAF_OTHERS -> {
                return "Ensurdecer membros"
            }
            Permission.VOICE_MOVE_OTHERS -> {
                return "Mover membros"
            }
            Permission.VOICE_USE_VAD -> {
                return "Usar deteção de voz"
            }
            Permission.NICKNAME_CHANGE -> {
                return "Mudar de nickname"
            }
            Permission.NICKNAME_MANAGE -> {
                return "Gerenciar nicknames"
            }
            Permission.MANAGE_WEBHOOKS -> {
                return "Gerenciar Webhooks"
            }
            Permission.MANAGE_EMOTES -> {
                return "Gerenciar Emojis"
            }
            Permission.USE_SLASH_COMMANDS -> {
                return "Usar commandos de /"
            }
            Permission.MESSAGE_ADD_REACTION -> {
                return "Adicionar reações"
            }
            Permission.MANAGE_ROLES -> {
                return "Gerenciar Cargos"
            }
            Permission.UNKNOWN -> {
                return "Desconhecido"
            }
        }
        /*
            REQUEST_TO_SPEAK	0x0100000000 (1 << 32)	Allows for requesting to speak in stage channels. (This permission is under active development and may be changed or removed.)	S
            MANAGE_THREADS *	0x0400000000 (1 << 34)	Allows for deleting and archiving threads, and viewing all private threads	T
            USE_PUBLIC_THREADS	0x0800000000 (1 << 35)	Allows for creating and participating in threads	T
            USE_PRIVATE_THREADS
        */
    }
}