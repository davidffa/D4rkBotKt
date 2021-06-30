package me.davidffa.d4rkbotkt

import com.mongodb.client.model.Updates
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory
import dev.minn.jda.ktx.injectKTX
import kotlinx.coroutines.runBlocking
import me.davidffa.d4rkbotkt.database.GuildCache
import me.davidffa.d4rkbotkt.events.EventManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent.*
import net.dv8tion.jda.api.utils.cache.CacheFlag
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.timerTask

class D4rkBot {
    companion object {
        lateinit var jda: JDA
        var commandsUsed = 0
        val guildCache = HashMap<Long, GuildCache>()
        val okHttpClient = OkHttpClient()

        private var lastCommandsUsed = 0

        suspend fun loadCache() {
            commandsUsed = Database.botDB.findOneById(jda.selfUser.id)!!.commands
            this.lastCommandsUsed = commandsUsed

            Timer().schedule(timerTask {
                if (lastCommandsUsed != commandsUsed) {
                    lastCommandsUsed = commandsUsed
                    runBlocking {
                        Database.botDB.updateOneById(jda.selfUser.id, Updates.set("commands", commandsUsed))
                    }
                }
            }, 30000)

            val dbGuilds = Database.guildDB.find().toList()

            jda.guilds.forEach { guild ->
                val dbGuild = dbGuilds.find { guild.id == it.id }

                if (dbGuild == null) {
                    guildCache[guild.idLong] = GuildCache("dk.")
                    return@forEach
                }

                guildCache[guild.idLong] = GuildCache(
                    dbGuild.prefix ?: "dk.",
                    dbGuild.disabledCmds,
                    dbGuild.autoRole,
                    dbGuild.welcomeChatID,
                    dbGuild.memberRemoveChatID,
                    dbGuild.djRole
                )
            }
        }
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
        logger.info(
            """
                 
            [36m    ____    __ __            __      ____           __        __     __ 
            [36m   / __ \  / // /   _____   / /__   / __ )  ____   / /_      / /__  / /_
            [36m  / / / / / // /_  / ___/  / //_/  / __  | / __ \ / __/     / //_/ / __/
            [36m / /_/ / /__  __/ / /     / ,<    / /_/ / / /_/ // /_   _  / ,<   / /_  
            [36m/_____/    /_/   /_/     /_/|_|  /_____/  \____/ \__/  (_)/_/|_|  \__/                                                                     
            [0m
            """.trimIndent()
        )

        try {
            jda = JDABuilder.create(System.getenv("TOKEN"), intents)
                .enableCache(CacheFlag.VOICE_STATE)
                .setStatus(OnlineStatus.IDLE)
                .setActivity(Activity.playing("A iniciar..."))
                .setAudioSendFactory(NativeAudioSendFactory())
                .injectKTX()
                .build()

            EventManager.manage(jda)
        }catch (e: Exception) {
            logger.error("Ocorreu um erro ao iniciar o D4rkBot.kt!", e)
        }
    }
}