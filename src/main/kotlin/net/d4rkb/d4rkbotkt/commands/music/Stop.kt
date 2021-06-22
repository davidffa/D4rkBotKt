package net.d4rkb.d4rkbotkt.commands.music

import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.d4rkb.d4rkbotkt.lavaplayer.PlayerManager
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Stop: Command(
    "stop",
    "Para de tocar música e limpa a lista de músicas.",
    listOf("parar", "disconnect", "desconectar", "quit", "leave", "sair"),
    category = "Music",
    botPermissions = listOf(Permission.MESSAGE_WRITE),
    cooldown = 4
) {
    override fun run(ctx: CommandContext) {
        if (!Utils.canUsePlayer(ctx.selfMember, ctx.member, ctx.channel)) return

        val musicManager = PlayerManager.getMusicManager(ctx.guild)

        musicManager.scheduler.queue.clear()
        musicManager.scheduler.player.stopTrack()
    }
}