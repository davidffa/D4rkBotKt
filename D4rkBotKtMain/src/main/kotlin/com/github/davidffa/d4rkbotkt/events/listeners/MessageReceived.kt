package com.github.davidffa.d4rkbotkt.events.listeners

import com.github.davidffa.d4rkbotkt.D4rkBot
import com.github.davidffa.d4rkbotkt.Translator
import com.github.davidffa.d4rkbotkt.command.CommandManager
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

suspend fun onMessageReceived(event: MessageReceivedEvent) {
  val user = event.author
  if (user.isBot || event.isWebhookMessage) return

  val prefix = D4rkBot.guildCache[event.guild.idLong]?.prefix ?: return //Returns if db has not been initialized yet
  val raw = event.message.contentRaw

  val mentionRegExp = Regex("^<@!?${event.jda.selfUser.id}>$")

  if (mentionRegExp.containsMatchIn(raw)) {
    if (event.guildChannel.canTalk()) {
      event.channel.sendMessage(
        Translator.t(
          "events.message",
          D4rkBot.guildCache[event.guild.idLong]!!.locale,
          listOf(user.asMention, prefix)
        )
      )
        .queue()
    }
    return
  }

  if (raw.startsWith(prefix)) {
    CommandManager.handle(event)
  }
}