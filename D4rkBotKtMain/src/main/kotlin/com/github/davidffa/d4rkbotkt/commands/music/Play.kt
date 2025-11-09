package com.github.davidffa.d4rkbotkt.commands.music

import com.github.davidffa.d4rkbotkt.audio.PlayerManager
import com.github.davidffa.d4rkbotkt.command.Command
import com.github.davidffa.d4rkbotkt.command.CommandContext
import com.github.davidffa.d4rkbotkt.utils.Utils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel

class Play : Command(
  "play",
  aliases = listOf("p", "tocar"),
  "Music",
  botPermissions = listOf(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS),
  cooldown = 2,
  args = 1
) {
  override suspend fun run(ctx: CommandContext) {
    val channel = ctx.channel
    val self = ctx.selfMember
    val selfVoiceState = self.voiceState
    val member = ctx.member

    if (!Utils.canPlay(ctx::t, ctx.selfMember, ctx.member, channel)) return

    if (!selfVoiceState!!.inAudioChannel()) {
      ctx.guild.audioManager.isSelfDeafened = true
      ctx.guild.audioManager.isSelfMuted = false
      ctx.guild.audioManager.openAudioConnection(member.voiceState?.channel as AudioChannel)
    }

    PlayerManager.loadAndPlay(member, channel, ctx.args.joinToString(" "))
  }
}