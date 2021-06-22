package net.d4rkb.d4rkbotkt.commands.music

import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.d4rkb.d4rkbotkt.lavaplayer.PlayerManager
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Skip: Command(
    "skip",
    "Pula a música atual.",
    listOf("s", "pular"),
    category = "Music",
    botPermissions = listOf(Permission.MESSAGE_WRITE),
    cooldown = 2
){
    override fun run(ctx: CommandContext) {
        if (!Utils.canUsePlayer(ctx.selfMember, ctx.member, ctx.channel)) return

        val musicManager = PlayerManager.getMusicManager(ctx.guild)

        if (!musicManager.scheduler.queue.isEmpty()) {
            ctx.channel.sendMessage(":fast_forward: Música pulada!").queue()
        }
        musicManager.scheduler.nextTrack()
    }
}