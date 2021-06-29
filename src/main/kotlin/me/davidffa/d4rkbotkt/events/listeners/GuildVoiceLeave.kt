package me.davidffa.d4rkbotkt.events.listeners

import me.davidffa.d4rkbotkt.lavaplayer.PlayerManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import java.util.*
import kotlin.concurrent.timerTask

fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
    val manager = PlayerManager.musicManagers[event.guild.idLong] ?: return

    val member = event.member

    if (member.idLong == event.jda.selfUser.idLong) {
        if (Utils.hasPermissions(member.guild.selfMember, manager.textChannel, listOf(Permission.MESSAGE_WRITE))) {
            manager.textChannel.sendMessage(":warning: Fui desconectado do canal de voz, por isso limpei a queue.")
                .queue()
        }
        manager.scheduler.destroy()
        return
    }

    val selfChannel = event.guild.audioManager.connectedChannel

    if (!member.user.isBot && selfChannel != null && event.oldValue.idLong == selfChannel.idLong && selfChannel.members.none { !it.user.isBot }) {
        manager.audioPlayer.isPaused = true

        if (Utils.hasPermissions(member.guild.selfMember, manager.textChannel, listOf(Permission.MESSAGE_WRITE))) {
            manager.textChannel.sendMessage(":warning: Pausei a música porque fiquei sozinho no canal de voz, se ninguem aparecer irei sair em 2 minutos.")
                .queue {
                    manager.leaveMessage = it
                }
        }

        val timer = Timer()
        manager.leaveTimer = timer

        timer.schedule(timerTask {
            if (Utils.hasPermissions(member.guild.selfMember, manager.textChannel, listOf(Permission.MESSAGE_WRITE))) {
                manager.textChannel.sendMessage(":x: Saí do canal de voz porque fiquei sozinho mais de 2 minutos.")
                    .queue()
                manager.scheduler.destroy()
            }
        }, 2 * 60 * 1000L)
    }
}