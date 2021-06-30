package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Resume : Command(
    "resume",
    "Retoma a música atual.",
    listOf("retomar"),
    category = "Music",
    cooldown = 4,
    botPermissions = listOf(Permission.MESSAGE_WRITE)
) {
    override suspend fun run(ctx: CommandContext) {
        if (!Utils.canUsePlayer(ctx.selfMember, ctx.member, ctx.channel)) return

        val musicManager = PlayerManager.getMusicManager(ctx.guild.idLong)

        if (musicManager.audioPlayer.isPaused) {
            ctx.channel.sendMessage(":play_pause: Música resumida!").queue()
            musicManager.audioPlayer.isPaused = false
        }else {
            ctx.channel.sendMessage(":x: A música já está a tocar!").queue()
        }
    }
}