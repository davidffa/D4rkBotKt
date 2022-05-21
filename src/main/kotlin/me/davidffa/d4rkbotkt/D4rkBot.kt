package me.davidffa.d4rkbotkt

import com.mongodb.client.model.Updates
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory
import dev.minn.jda.ktx.jdabuilder.createJDA
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.davidffa.d4rkbotkt.database.BotDB
import me.davidffa.d4rkbotkt.database.GuildCache
import me.davidffa.d4rkbotkt.events.EventManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent.*
import net.dv8tion.jda.api.utils.cache.CacheFlag
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class D4rkBot {
  companion object {
    private lateinit var jda: JDA
    var commandsUsed = 0
    val guildCache = HashMap<Long, GuildCache>()
    val okHttpClient = OkHttpClient()

    private var lastCommandsUsed = 0

    suspend fun loadCache() {
      if (!Database.isDBInitialized()) delay(1000)

      var botDB = Database.botDB.findOneById(jda.selfUser.id)

      if (botDB == null) {
        botDB = BotDB(jda.selfUser.id, 0, listOf(), listOf())
        Database.botDB.insertOne(botDB)
      }

      commandsUsed = botDB.commands
      this.lastCommandsUsed = commandsUsed

      val threadPool = Executors.newSingleThreadScheduledExecutor()

      threadPool.scheduleWithFixedDelay({
        if (lastCommandsUsed != commandsUsed) {
          lastCommandsUsed = commandsUsed
          runBlocking {
            Database.botDB.updateOneById(jda.selfUser.id, Updates.set("commands", commandsUsed))
          }
        }
      }, 0, 30, TimeUnit.SECONDS)

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
          dbGuild.welcomeMessagesEnabled,
          dbGuild.welcomeChatID,
          dbGuild.memberRemoveMessagesEnabled,
          dbGuild.memberRemoveChatID,
          dbGuild.djRole,
          if (dbGuild.lang == "en") Locale.EN else Locale.PT
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
    GUILD_MESSAGE_REACTIONS
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
      jda = createJDA(Credentials.TOKEN, true, intents = intents) {
        this.enableCache(listOf(CacheFlag.VOICE_STATE, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS))
        this.disableCache(listOf(CacheFlag.ACTIVITY, CacheFlag.ROLE_TAGS))
        this.setAudioSendFactory(NativeAudioSendFactory())

        this.setStatus(OnlineStatus.IDLE)
        this.setActivity(Activity.playing("A iniciar..."))
      }

      EventManager.manage(jda)
    } catch (e: Exception) {
      logger.error("Ocorreu um erro ao iniciar o D4rkBot.kt!", e)
    }
  }
}