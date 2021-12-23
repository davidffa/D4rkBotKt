package me.davidffa.d4rkbotkt.events.listeners

import com.mongodb.client.model.Updates
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.Translator
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent

suspend fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
  val cache = D4rkBot.guildCache[event.guild.idLong]!!
  val memberRemoveChatID = cache.memberRemoveChatID

  if (cache.memberRemoveMessagesEnabled != true) return

  if (memberRemoveChatID != null) {
    val channel = event.guild.getGuildChannelById(memberRemoveChatID) as TextChannel?

    if (channel == null) {
      cache.memberRemoveChatID = null
      cache.memberRemoveMessagesEnabled = false
      Database.guildDB.updateOneById(
        event.guild.id,
        Updates.combine(Updates.set("memberRemoveChatID", null), Updates.set("memberRemoveMessagesEnabled", false))
      )
      return
    }

    if (!Utils.hasPermissions(event.guild.selfMember, channel, listOf(Permission.MESSAGE_SEND))) return

    val tag = event.member?.user?.asTag ?: return

    channel.sendMessage(Translator.t("events.leave", cache.locale, listOf(tag))).queue()
  }
}