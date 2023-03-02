package me.davidffa.d4rkbotkt.events

import dev.minn.jda.ktx.events.listener
import me.davidffa.d4rkbotkt.events.listeners.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent

object EventManager {
  fun manage(jda: JDA) {
    jda.listener<GenericEvent> { event ->
      when (event) {
        is ReadyEvent -> onReady(event)
        is MessageReceivedEvent -> onMessageReceived(event)
        is GuildVoiceUpdateEvent -> {
          if (event.oldValue != null && event.newValue != null) {
            onGuildVoiceMove(event)
          } else if (event.oldValue == null && event.newValue != null) {
            onGuildVoiceJoin(event)
          } else if (event.oldValue != null && event.newValue == null) {
            onGuildVoiceLeave(event)
          }
        }
        is GuildJoinEvent -> onGuildJoin(event)
        is GuildLeaveEvent -> onGuildLeave(event)
        is GuildMemberJoinEvent -> onGuildMemberJoin(event)
        is GuildMemberRemoveEvent -> onGuildMemberRemove(event)
        is MessageDeleteEvent -> onMessageDelete(event)
      }
    }
  }
}