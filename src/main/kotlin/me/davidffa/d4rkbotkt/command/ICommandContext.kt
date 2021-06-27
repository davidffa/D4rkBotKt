package me.davidffa.d4rkbotkt.command

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

interface ICommandContext {
    val event: GuildMessageReceivedEvent

    val guild: Guild
        get() = this.event.guild
    val channel: TextChannel
        get() = this.event.channel
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
}