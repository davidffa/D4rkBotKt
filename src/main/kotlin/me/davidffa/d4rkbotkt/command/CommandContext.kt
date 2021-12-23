package me.davidffa.d4rkbotkt.command

import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class CommandContext(override val event: MessageReceivedEvent, val args: List<String>, val prefix: String) :
  ICommandContext