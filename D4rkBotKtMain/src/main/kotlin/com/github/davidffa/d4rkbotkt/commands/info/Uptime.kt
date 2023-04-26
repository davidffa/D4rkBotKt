package com.github.davidffa.d4rkbotkt.commands.info

import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.lang.management.ManagementFactory

class Uptime : Command(
  "uptime",
  listOf("ontime"),
  botPermissions = listOf(Permission.MESSAGE_SEND),
  category = "Info",
  cooldown = 3
) {
  override suspend fun run(ctx: CommandContext) {
    ctx.channel.sendMessage("<a:infinity:838759634361253929> Uptime: `${Utils.msToDate(ManagementFactory.getRuntimeMXBean().uptime)}`")
      .queue()
  }
}