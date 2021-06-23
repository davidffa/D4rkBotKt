package net.d4rkb.d4rkbotkt.commands.info

import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import java.time.Instant

class Avatar : Command(
"avatar",
    "Mostra o teu avatar ou de outra pessoa em uma imagem grande.",
    listOf("av"),
    "[ID/Nome]",
    "Info",
    botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
    cooldown = 3,
    dm = true
) {
    override fun run(ctx: CommandContext) {
        val user = if (ctx.args.isEmpty()) ctx.author
        else Utils.findUser(ctx.args.joinToString(" "), ctx.guild)

        if (user == null) {
            ctx.channel.sendMessage(":x: Utilizador não encontrado!").queue()
            return
        }

        val url = "${user.effectiveAvatarUrl}?size=4096"

        val embed = EmbedBuilder()
            .setTitle(":frame_photo: Avatar de ${user.name}#${user.discriminator}")
            .setDescription(":diamond_shape_with_a_dot_inside: Clique [aqui](${url}) para baixar a imagem!")
            .setColor(Utils.randColor())
            .setImage(url)
            .setFooter(ctx.author.asTag, ctx.author.effectiveAvatarUrl)
            .setTimestamp(Instant.now())
            .build()

        ctx.channel.sendMessageEmbeds(embed).queue()
    }
}