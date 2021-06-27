package me.davidffa.d4rkbotkt.events.listeners

import me.davidffa.d4rkbotkt.lavaplayer.PlayerManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.util.*
import kotlin.concurrent.timerTask

private val logger = LoggerFactory.getLogger("Ready")

fun onReady(event: ReadyEvent) {
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
                presence.setPresence(OnlineStatus.ONLINE, Activity.watching("x comandos executados"))
                id = -1
            }
        }
        id++
    }, 0, 30000)

    logger.info("D4rkBot.kt iniciado")
    logger.info("Utilizadores: ${event.jda.users.size}")
    logger.info("Servidores: ${event.jda.guilds.size}")
}