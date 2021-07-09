package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import kotlin.math.pow

class Seek : Command(
    "seek",
    "Avança para um tempo específico da música.",
    usage = "<Tempo>",
    category = "Music",
    botPermissions = listOf(Permission.MESSAGE_WRITE),
    args = 1,
    cooldown = 2
) {
    override suspend fun run(ctx: CommandContext) {
        if (!Utils.canUsePlayer(ctx.selfMember, ctx.member, ctx.channel, true)) return

        val track = PlayerManager.getMusicManager(ctx.guild.idLong).scheduler.current.track

        var time = 0L

        val chunks = ctx.args[0].split(":")

        for (i in chunks.indices) {
            val num = chunks[chunks.size - i - 1].toLongOrNull()

            if (num == null) {
                ctx.channel.sendMessage(":x: Formato inválido! Tenta da forma `hh:mm:ss`, `mm:ss` ou `ss`").queue()
                return
            }

            time += num * 60.0.pow(i).toLong()
        }

        time *= 1000

        if (time > track.duration) {
            ctx.channel.sendMessage(":x: O tempo só pode variar entre **0** e **${Utils.msToHour(track.duration)}**.").queue()
            return
        }

        track.position = time

        ctx.channel.sendMessage(":fast_forward: Posição da música setada para `${Utils.msToHour(time)}`.").queue()
    }
}