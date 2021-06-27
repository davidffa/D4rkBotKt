package me.davidffa.d4rkbotkt.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class CommandContext(override val event: GuildMessageReceivedEvent, val args: List<String>): ICommandContext