package net.d4rkb.d4rkbotkt

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import net.d4rkb.d4rkbotkt.database.BotDB
import net.d4rkb.d4rkbotkt.utils.GuildCache
import net.dv8tion.jda.api.GatewayEncoding
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent.*
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.timerTask

object D4rkBot {
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

    val jda = JDABuilder.create(System.getenv("TOKEN"), intents)
        .setGatewayEncoding(GatewayEncoding.ETF)
        .addEventListeners(EventListener())
        .enableCache(CacheFlag.VOICE_STATE)
        .build()

    var commandsUsed = 0
    private var lastCommandsUsed = 0

    val guildCache = HashMap<String, GuildCache>()

    fun loadCache() {
        this.commandsUsed = Database.botDB.find(Filters.eq("_id", this.jda.selfUser.id)).first()!!.commands
        this.lastCommandsUsed = this.commandsUsed

        Timer().schedule(timerTask {
            if (lastCommandsUsed != commandsUsed) {
                lastCommandsUsed = commandsUsed
                Database.botDB.updateOne(Filters.eq("_id", jda.selfUser.id), Updates.set("commands", commandsUsed))
            }
        }, 30000)

        /* Guild Stuff */
        val dbGuilds = Database.guildDB.find()

        jda.guilds.forEach { guild ->
            val dbGuild = dbGuilds.find { guild.id == it.id }

            if (dbGuild == null) {
                this.guildCache[guild.id] = GuildCache("dk.")
                return@forEach
            }

            this.guildCache[guild.id] = GuildCache(
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