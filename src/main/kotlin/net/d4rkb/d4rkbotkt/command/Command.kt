package net.d4rkb.d4rkbotkt.command

import net.dv8tion.jda.api.Permission

abstract class Command(
    open val name: String,
    val description: String,
    val aliases: List<String> = listOf(),
    val usage: String = "",
    val category: String = "Others",
    val userPermissions: List<Permission> = listOf(),
    val botPermissions: List<Permission> = listOf(),
    val args: Byte = 0,
    val cooldown: Byte = 3,
    val dm: Boolean = false,
) {
    abstract fun run(ctx: CommandContext)
}