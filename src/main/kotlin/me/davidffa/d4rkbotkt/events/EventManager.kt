package me.davidffa.d4rkbotkt.events

import dev.minn.jda.ktx.events.listener
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
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object EventManager {
  fun manage(jda: JDA) {
    jda.listener<GenericEvent> { event ->
      when (event) {
        is ReadyEvent -> onReady(event)
        is MessageReceivedEvent -> onMessageReceived(event)
        is GuildVoiceJoinEvent -> onGuildVoiceJoin(event)
        is GuildVoiceLeaveEvent -> onGuildVoiceLeave(event)
        is GuildVoiceMoveEvent -> onGuildVoiceMove(event)
        is GuildJoinEvent -> onGuildJoin(event)
        is GuildLeaveEvent -> onGuildLeave(event)
        is GuildMemberJoinEvent -> onGuildMemberJoin(event)
        is GuildMemberRemoveEvent -> onGuildMemberRemove(event)
        is MessageDeleteEvent -> onMessageDelete(event)
      }
    }
  }
}