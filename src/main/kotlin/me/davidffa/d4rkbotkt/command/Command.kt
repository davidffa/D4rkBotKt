package me.davidffa.d4rkbotkt.command

import net.dv8tion.jda.api.Permission

abstract class Command(
  val name: String,
  val description: String,
  val aliases: List<String>? = null,
  val usage: String? = null,
  val category: String = "Others",
  val botPermissions: List<Permission>? = null,
  val userPermissions: List<Permission>? = null,
  val args: Byte = 0,
  val cooldown: Byte = 3
) {
  abstract suspend fun run(ctx: CommandContext)
}