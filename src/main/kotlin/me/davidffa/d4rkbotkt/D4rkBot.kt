package me.davidffa.d4rkbotkt

import dev.minn.jda.ktx.injectKTX
import me.davidffa.d4rkbotkt.events.EventManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent.*
import net.dv8tion.jda.api.utils.cache.CacheFlag
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory

class D4rkBot {
    companion object {
        lateinit var instance: D4rkBot
        lateinit var jda: JDA
        val okHttpClient = OkHttpClient()
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val intents = listOf(
        GUILD_MEMBERS,
        GUILD_EMOJIS,
        GUILD_VOICE_STATES,
        GUILD_PRESENCES,
        GUILD_MESSAGES,
        GUILD_MESSAGE_REACTIONS,
        DIRECT_MESSAGES,
        DIRECT_MESSAGE_REACTIONS,
    )

    init {
        instance = this

        try {
            jda = JDABuilder.create(System.getenv("TOKEN"), intents)
                .enableCache(CacheFlag.VOICE_STATE)
                .setStatus(OnlineStatus.IDLE)
                .setActivity(Activity.playing("A iniciar..."))
                .injectKTX()
                .build()

            EventManager.manage(jda)
        }catch (e: Exception) {
            logger.error("Ocorreu um erro ao iniciar o D4rkBot.kt!", e)
        }
    }
}