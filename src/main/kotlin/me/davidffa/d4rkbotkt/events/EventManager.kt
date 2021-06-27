package me.davidffa.d4rkbotkt.events

import dev.minn.jda.ktx.listener
import me.davidffa.d4rkbotkt.events.listeners.onGuildMessageReceived
import me.davidffa.d4rkbotkt.events.listeners.onReady
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object EventManager {
    fun manage(jda: JDA) {
        jda.listener<GenericEvent> { event ->
            when (event) {
                is ReadyEvent -> onReady(event)
                is GuildMessageReceivedEvent -> onGuildMessageReceived(event)
            }
        }
    }
}