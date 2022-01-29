package me.davidffa.d4rkbotkt.command

import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.Translator
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface ICommandContext {
  val event: MessageReceivedEvent

  val guild: Guild
    get() = this.event.guild
  val channel: GuildMessageChannel
    get() = this.event.channel as GuildMessageChannel
  val message: Message
    get() = this.event.message
  val author: User
    get() = this.event.author
  val member: Member
    get() = this.event.member!!
  val jda: JDA
    get() = this.event.jda
  val selfUser: SelfUser
    get() = this.jda.selfUser
  val selfMember: Member
    get() = this.guild.selfMember

  fun t(path: String, placeholders: List<String>? = null): String {
    return Translator.t(
      path,
      D4rkBot.guildCache[guild.idLong]!!.locale,
      placeholders
    )
  }
}