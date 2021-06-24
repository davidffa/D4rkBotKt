package net.d4rkb.d4rkbotkt.commands.music

import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.d4rkb.d4rkbotkt.lavaplayer.PlayerManager
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Pause : Command(
    "pause",
    "Pausa a música atual.",
    listOf("pausa"),
    category = "Music",
    botPermissions = listOf(Permission.MESSAGE_WRITE),
    cooldown = 4
) {
    override fun run(ctx: CommandContext) {
        if (!Utils.canUsePlayer(ctx.selfMember, ctx.member, ctx.channel)) return

        val musicManager = PlayerManager.getMusicManager(ctx.guild)

        if (musicManager.audioPlayer.isPaused) {
            ctx.channel.sendMessage(":play_pause: Música resumida!").queue()
            musicManager.audioPlayer.isPaused = false
        }else {
            ctx.channel.sendMessage(":pause_button: Música pausada!").queue()
            musicManager.audioPlayer.isPaused = true
        }
    }
}