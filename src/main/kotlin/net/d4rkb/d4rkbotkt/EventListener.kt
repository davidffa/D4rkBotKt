package net.d4rkb.d4rkbotkt

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import net.d4rkb.d4rkbotkt.command.CommandManager
import net.d4rkb.d4rkbotkt.lavaplayer.PlayerManager
import net.d4rkb.d4rkbotkt.utils.GuildCache
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.util.*
import kotlin.concurrent.timerTask

class EventListener : ListenerAdapter() {
    private val logger = LoggerFactory.getLogger("EventListener")
    private val manager = CommandManager()

    override fun onReady(event: ReadyEvent) {
        D4rkBot.loadCache()

        val jda = event.jda
        val presence = jda.presence
        var id: Byte = 0
        Timer().schedule(timerTask {
            when (id) {
                (0).toByte() -> {
                    presence.setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.watching("D4rkB#2408"))
                }
                (1).toByte() -> {
                    presence.setPresence(OnlineStatus.ONLINE, Activity.competing("${jda.guilds.size} Servidores"))
                }
                (2).toByte() -> {
                    presence.setPresence(OnlineStatus.ONLINE, Activity.watching("${jda.users.size} Utilizadores"))
                }
                (3).toByte() -> {
                    val totalChannels = jda.storeChannels.size + jda.textChannels.size + jda.voiceChannels.size + jda.categories.size
                    presence.setPresence(OnlineStatus.ONLINE, Activity.streaming("$totalChannels Canais", "https://twitch.tv/d4rkb12"))
                }
                (4).toByte() -> {
                    presence.setPresence(OnlineStatus.ONLINE, Activity.playing("@D4rkBot.kt"))
                }
                (5).toByte() -> {
                    presence.setPresence(OnlineStatus.ONLINE, Activity.listening("${PlayerManager.musicManagers.size} músicas"))
                }
                (6).toByte() -> {
                    val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
                    presence.setPresence(OnlineStatus.ONLINE, Activity.streaming("Online há ${Utils.msToDate(runtimeMXBean.uptime)}", "https://twitch.tv/d4rkb12"))
                }
                else -> {
                    presence.setPresence(OnlineStatus.ONLINE, Activity.watching("${D4rkBot.commandsUsed} comandos executados"))
                    id = -1
                }
            }
            id++
        }, 0, 30000)

        logger.info("D4rkBot.kt iniciado")
        logger.info("Utilizadores: ${event.jda.users.size}")
        logger.info("Servidores: ${event.jda.guilds.size}")
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val user = event.author
        if (user.isBot || event.isWebhookMessage) return

        val prefix = D4rkBot.guildCache[event.guild.id]!!.prefix
        val raw = event.message.contentRaw

        val mentionRegExp = Regex("^<@!?${event.jda.selfUser.id}>$")

        if (mentionRegExp.containsMatchIn(raw)) {
            val botPermissions = event.guild.selfMember.getPermissions(event.channel)
            if (botPermissions.contains(Permission.MESSAGE_WRITE)) {
                event.channel.sendMessage("<a:blobcool:804026346954555432> Olá ${user.asMention} O meu prefixo neste servidor é `${prefix}`. Faz `${prefix}help` para veres o que posso fazer!")
                    .queue()
            }
            return
        }

        if (raw.startsWith(prefix)) {
            manager.handle(event)
        }
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        D4rkBot.guildCache[event.guild.id] = GuildCache("dk.")
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        D4rkBot.guildCache.remove(event.guild.id)
        Database.guildDB.findOneAndDelete(Filters.eq("_id", event.guild.id))
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val cache = D4rkBot.guildCache[event.guild.id]!!

        if (cache.welcomeChatID != null) {
            val channel = event.guild.channels.find { it.id == cache.welcomeChatID } as TextChannel?

            if (channel == null) {
                cache.welcomeChatID = null
                Database.guildDB.updateOne(Filters.eq("_id", event.guild.id), Updates.set("welcomeChatID", null))
                return
            }

            if (event.guild.selfMember.getPermissions(channel).contains(Permission.MESSAGE_WRITE)) {
                // In future do a canvas image ?? :>
                channel.sendMessage("`${event.user.asTag}` Bem-Vindo ao servidor ${event.guild.name}").queue()
            }
        }

        if (cache.autoRole != null) {
            val role = event.guild.roles.find { it.id == cache.autoRole }

            if (role == null) {
                cache.autoRole = null
                Database.guildDB.updateOne(Filters.eq("_id", event.guild.id), Updates.set("autoRole", null))
                return
            }

            var botHighestRole = event.guild.selfMember.roles[0]

            event.guild.selfMember.roles.forEach {
                if (it.position > botHighestRole.position) botHighestRole = it
            }

            if (event.guild.selfMember.permissions.contains(Permission.MANAGE_ROLES) && botHighestRole.position > role.position) {
                event.guild.addRoleToMember(event.member.id, role)
            }
        }
    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        val cache = D4rkBot.guildCache[event.guild.id]!!

        if (cache.memberRemoveChatID != null) {
            val channel = event.guild.channels.find { it.id == cache.memberRemoveChatID } as TextChannel?

            if (channel == null) {
                cache.memberRemoveChatID = null
                Database.guildDB.updateOne(Filters.eq("_id", event.guild.id), Updates.set("memberRemoveChatID", null))
                return
            }

            if (event.guild.selfMember.getPermissions(channel).contains(Permission.MESSAGE_WRITE)) {
                channel.sendMessage("`${event.user.asTag}` saiu do servidor.").queue()
            }
        }
    }
}