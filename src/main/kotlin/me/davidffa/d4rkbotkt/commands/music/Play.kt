package me.davidffa.d4rkbotkt.commands.music

import me.davidffa.d4rkbotkt.audio.PlayerManager
import me.davidffa.d4rkbotkt.audio.receive.ReceiverManager
import me.davidffa.d4rkbotkt.command.Command
import me.davidffa.d4rkbotkt.command.CommandContext
import me.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission

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
  override suspend fun run(ctx: CommandContext) {
    val channel = ctx.channel
    val self = ctx.selfMember
    val selfVoiceState = self.voiceState
    val member = ctx.member

    if (!Utils.canPlay(ctx.selfMember, ctx.member, channel)) return

    if (!selfVoiceState!!.inVoiceChannel()) {
      ctx.guild.audioManager.isSelfDeafened = true
      ctx.guild.audioManager.openAudioConnection(member.voiceState?.channel)
    }else if (ReceiverManager.receiveManagers.contains(ctx.guild.idLong)) {
      ctx.guild.audioManager.isSelfMuted = false
    }

    var link = ctx.args.joinToString(" ")

    if (!Utils.isUrl(link)) {
      link = "ytsearch:$link"
    }

    PlayerManager.loadAndPlay(member, channel, link)
  }
}