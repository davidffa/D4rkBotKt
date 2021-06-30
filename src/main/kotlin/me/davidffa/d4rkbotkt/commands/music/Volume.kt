package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

class Volume : Command(
    "volume",
    "Altera o volume da música.",
    listOf("vol"),
    "[valor]",
    "Music",
    listOf(Permission.MESSAGE_WRITE),
    cooldown = 4
){
    override suspend fun run(ctx: CommandContext) {
        if (!Utils.canUsePlayer(ctx.selfMember, ctx.member, ctx.channel, true)) return

        val manager = PlayerManager.getMusicManager(ctx.guild.idLong)

        if (ctx.args.isEmpty()) {
            ctx.channel.sendMessage(":speaker: Volume atual: `${manager.volume * 100}`").queue()
            return
        }

        val vol = ctx.args[0].toIntOrNull()

        if (vol == null || vol < 0 || vol > 200) {
            ctx.channel.sendMessage(":x: O volume tem de variar entre 0 e 200").queue()
            return
        }

        manager.volume = vol / 100f
        ctx.channel.sendMessage(":speaker: Volume alterado para `${manager.volume * 100}`").queue()
    }
}