package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.lavaplayer.PlayerManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Skip: Command(
    "skip",
    "Pula a música atual.",
    listOf("s", "pular"),
    category = "Music",
    botPermissions = listOf(Permission.MESSAGE_WRITE),
    cooldown = 2
){
    override suspend fun run(ctx: CommandContext) {
        if (!Utils.canUsePlayer(ctx.selfMember, ctx.member, ctx.channel)) return

        val musicManager = PlayerManager.getMusicManager(ctx.guild.idLong)

        if (musicManager.scheduler.queue.isNotEmpty()) {
            ctx.channel.sendMessage(":fast_forward: Música pulada!").queue()
        }
        musicManager.scheduler.nextTrack()
    }
}