package com.github.davidffa.d4rkbotkt.audio

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class AudioPlayerSendHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
  private val buffer = ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize())
  private val frame = MutableAudioFrame()

  init {
    this.frame.setBuffer(buffer)
  }

  override fun canProvide() = this.audioPlayer.provide(this.frame)

  override fun provide20MsAudio(): ByteBuffer = this.buffer.flip()

  override fun isOpus() = true
}