package me.davidffa.d4rkbotkt.events.listeners

import com.mongodb.client.model.Updates
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Database
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent

suspend fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
  val cache = D4rkBot.guildCache[event.guild.idLong]!!
  val welcomeChatID = cache.welcomeChatID

  if (welcomeChatID != null) {
    val channel = event.guild.getGuildChannelById(welcomeChatID) as TextChannel?

    if (channel == null) {
      cache.welcomeChatID = null
      Database.guildDB.updateOneById(event.guild.id, Updates.set("welcomeChatID", null))
      return
    }

    if (!Utils.hasPermissions(event.guild.selfMember, channel, listOf(Permission.MESSAGE_WRITE))) return
    channel.sendMessage(":tada: `${event.member.user.asTag}` bem-vindo ao servidor `${event.guild.name}`.").queue()
  }
}