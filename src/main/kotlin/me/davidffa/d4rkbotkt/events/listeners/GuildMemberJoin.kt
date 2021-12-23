package me.davidffa.d4rkbotkt.events.listeners

import com.mongodb.client.model.Updates
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.Translator
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent

suspend fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
  val cache = D4rkBot.guildCache[event.guild.idLong]!!
  val welcomeChatID = cache.welcomeChatID

  if (cache.welcomeMessagesEnabled != true) return

  if (welcomeChatID != null) {
    val channel = event.guild.getGuildChannelById(welcomeChatID) as TextChannel?

    if (channel == null) {
      cache.welcomeChatID = null
      cache.welcomeMessagesEnabled = false
      Database.guildDB.updateOneById(
        event.guild.id,
        Updates.combine(Updates.set("welcomeChatID", null), Updates.set("welcomeMessagesEnabled", false))
      )
      return
    }

    if (!Utils.hasPermissions(event.guild.selfMember, channel, listOf(Permission.MESSAGE_SEND))) return
    channel.sendMessage(Translator.t("events.welcome", cache.locale, listOf(event.member.user.asTag, event.guild.name)))
      .queue()
  }
}