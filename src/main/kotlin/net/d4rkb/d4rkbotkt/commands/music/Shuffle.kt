package net.d4rkb.d4rkbotkt.commands.music

import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.d4rkb.d4rkbotkt.lavaplayer.PlayerManager
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Shuffle : Command(
    "shuffle",
    "Embaralha a lista de músicas.",
    category = "Music",
    cooldown = 8,
    botPermissions = listOf(Permission.MESSAGE_WRITE),
) {
    override fun run(ctx: CommandContext) {
        if (!Utils.canUsePlayer(ctx.selfMember, ctx.member, ctx.channel)) return

        val musicManager = PlayerManager.getMusicManager(ctx.guild)

        if (musicManager.scheduler.queue.isEmpty()) {
            ctx.channel.sendMessage(":x: A lista de músicas está vazia!").queue()
            return
        }

        musicManager.scheduler.shuffle()
        ctx.channel.sendMessage(":minidisc: Lista de músicas embaralhada!").queue()
    }
}