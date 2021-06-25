package net.d4rkb.d4rkbotkt.commands.music

import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.d4rkb.d4rkbotkt.lavaplayer.PlayerManager
import net.dv8tion.jda.api.Permission

class Nowplaying : Command(
    "nowplaying",
    "Mostra a música que está a tocar.",
    listOf("np", "tocando"),
    botPermissions = listOf(Permission.MESSAGE_WRITE),
    category = "Music",
    cooldown = 5,

) {
    override fun run(ctx: CommandContext) {
        val musicManager = PlayerManager.musicManagers[ctx.guild.idLong]

        if (musicManager == null) {
            ctx.channel.sendMessage(":x: Não estou a tocar nada de momento!").queue()
            return
        }

        ctx.channel.sendMessage("<a:disco:803678643661832233> A tocar: `${musicManager.scheduler.currentTrack?.track
            ?.info?.title}`").queue()
    }
}