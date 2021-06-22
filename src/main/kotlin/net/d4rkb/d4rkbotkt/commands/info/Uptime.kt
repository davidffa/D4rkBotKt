package net.d4rkb.d4rkbotkt.commands.info

import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.lang.management.ManagementFactory

class Uptime: Command(
    "uptime",
    "Mostra à quanto tempo estou online.",
    listOf("ontime"),
    botPermissions = listOf(Permission.MESSAGE_WRITE),
    category = "Info",
    cooldown = 3,
    dm = true
) {
    override fun run(ctx: CommandContext) {
        ctx.channel.sendMessage("<a:infinity:838759634361253929> Uptime: `${Utils.msToDate(ManagementFactory.getRuntimeMXBean().uptime)}`")
            .queue()
    }
}