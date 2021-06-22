package net.d4rkb.d4rkbotkt

import net.dv8tion.jda.api.GatewayEncoding
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent.*
import net.dv8tion.jda.api.utils.cache.CacheFlag
import javax.security.auth.login.LoginException

class D4rkBot @Throws(LoginException::class) constructor() {
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
}