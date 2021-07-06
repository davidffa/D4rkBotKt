package me.davidffa.d4rkbotkt.events.listeners

import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.command.CommandManager
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

suspend fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
    val user = event.author
    if (user.isBot || event.isWebhookMessage) return

    val prefix = D4rkBot.guildCache[event.guild.idLong]?.prefix ?: return //Returns if db has not been initialized yet
    val raw = event.message.contentRaw

    val mentionRegExp = Regex("^<@!?${event.jda.selfUser.id}>$")

    if (mentionRegExp.containsMatchIn(raw)) {
        val botPermissions = event.guild.selfMember.getPermissions(event.channel)
        if (botPermissions.contains(Permission.MESSAGE_WRITE)) {
            event.channel.sendMessage("<a:blobcool:804026346954555432> Olá ${user.asMention} O meu prefixo neste servidor é `${prefix}`. Faz `${prefix}help` para veres o que posso fazer!")
                .queue()
        }
        return
    }

    if (raw.startsWith(prefix)) {
        CommandManager.handle(event)
    }
}