package net.d4rkb.d4rkbotkt.utils

data class GuildCache(
    var prefix: String,
    val disabledCommands: List<String>? = null,
    var autoRole: String? = null,
    var welcomeChatID: String? = null,
    var memberRemoveChatID: String? = null,
    var djRole: String? = null
)