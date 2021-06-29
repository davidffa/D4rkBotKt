package me.davidffa.d4rkbotkt.events

import dev.minn.jda.ktx.listener
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.database.GuildCache
import me.davidffa.d4rkbotkt.events.listeners.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object EventManager {
    fun manage(jda: JDA) {
        jda.listener<GenericEvent> { event ->
            when (event) {
                is ReadyEvent -> onReady(event)
                is GuildMessageReceivedEvent -> onGuildMessageReceived(event)
                is GuildVoiceJoinEvent -> onGuildVoiceJoin(event)
                is GuildVoiceLeaveEvent -> onGuildVoiceLeave(event)
                is GuildVoiceMoveEvent -> onGuildVoiceMove(event)
                is GuildJoinEvent -> onGuildJoin(event)
                is GuildLeaveEvent -> onGuildLeave(event)
                is GuildMemberJoinEvent -> onGuildMemberJoin(event)
                is GuildMemberRemoveEvent -> onGuildMemberRemove(event)
            }
        }
    }

    private fun onGuildJoin(event: GuildJoinEvent) {
        D4rkBot.guildCache[event.guild.idLong] = GuildCache("dk.")
    }

    private suspend fun onGuildLeave(event: GuildLeaveEvent) {
        D4rkBot.guildCache.remove(event.guild.idLong)
        Database.guildDB.deleteOneById(event.guild.id)
    }
}