package me.davidffa.d4rkbotkt.events.listeners

import com.mongodb.client.model.Updates
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent

suspend fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
  val cache = D4rkBot.guildCache[event.guild.idLong]!!
  val memberRemoveChatID = cache.memberRemoveChatID

  if (memberRemoveChatID != null) {
    val channel = event.guild.getGuildChannelById(memberRemoveChatID) as TextChannel?

    if (channel == null) {
      cache.memberRemoveChatID = null
      Database.guildDB.updateOneById(event.guild.id, Updates.set("memberRemoveChatID", null))
      return
    }

    if (!Utils.hasPermissions(event.guild.selfMember, channel, listOf(Permission.MESSAGE_WRITE))) return
    channel.sendMessage(":door: `${event.member?.user?.asTag}` saiu do servidor.").queue()
  }
}