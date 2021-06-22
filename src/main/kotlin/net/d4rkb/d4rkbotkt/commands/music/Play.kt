package net.d4rkb.d4rkbotkt.commands.music

import net.d4rkb.d4rkbotkt.command.Command
import net.d4rkb.d4rkbotkt.command.CommandContext
import net.d4rkb.d4rkbotkt.lavaplayer.PlayerManager
import net.d4rkb.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import java.net.MalformedURLException
import java.net.URL

class Play : Command(
  "play",
    "Toca uma música ou adiciona-a na lista.",
    aliases = listOf("p", "tocar"),
    "<Nome/URL>",
    "Music",
    botPermissions = listOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS),
    cooldown = 2,
    args = 1
) {
    override fun run(ctx: CommandContext) {
        val channel = ctx.channel
        val self = ctx.selfMember
        val selfVoiceState = self.voiceState
        val member = ctx.member

        if (!Utils.canPlay(ctx.selfMember, ctx.member, channel)) return

        if (!selfVoiceState!!.inVoiceChannel()) {
            ctx.guild.audioManager.openAudioConnection(member.voiceState?.channel)
        }

        var link = ctx.args.joinToString(" ")

        if (!Utils.isUrl(link)) {
            link = "ytsearch:$link"
        }

        PlayerManager.loadAndPlay(member, channel, link)
    }
}