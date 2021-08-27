package me.davidffa.d4rkbotkt.database

import me.davidffa.d4rkbotkt.Locale

data class GuildCache(
  var prefix: String,
  val disabledCommands: List<String>? = null,
  var autoRole: String? = null,
  var welcomeMessagesEnabled: Boolean? = null,
  var welcomeChatID: String? = null,
  var memberRemoveMessagesEnabled: Boolean? = null,
  var memberRemoveChatID: String? = null,
  var djRole: String? = null,
  var locale: Locale = Locale.PT
)
