package com.github.davidffa.d4rkbotkt.audio.receive

import net.dv8tion.jda.api.audio.AudioReceiveHandler
import net.dv8tion.jda.api.audio.CombinedAudio
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.min

class AudioReceiver(
  guildId: String,
  bitrate: Int
) : AudioReceiveHandler {
  private val volume = 1.0

  private val ffmpeg: Process

  override fun canReceiveEncoded() = false
  override fun canReceiveUser() = false
  override fun canReceiveCombined() = true

  init {
    Files.createDirectories(Paths.get("./records"))

    // Audio format: AudioSendHandler.INPUT_FORMAT (48k 16 bit big endian pcm)
    val args = arrayOf("-f", "s16be", "-ar", "48k", "-ac", "2", "-i", "pipe:0", "-b:a", min(bitrate, 128000).toString(), "-f", "mp3", "pipe:1")

    ffmpeg = ProcessBuilder("ffmpeg", *args)
      .redirectError(ProcessBuilder.Redirect.DISCARD)  // Logs
      .redirectInput(ProcessBuilder.Redirect.PIPE)
      .redirectOutput(File("./records/record-${guildId}.mp3"))
      .start()
  }

  override fun handleCombinedAudio(combinedAudio: CombinedAudio) {
    ffmpeg.outputStream.write(combinedAudio.getAudioData(volume))
  }

  fun close() {
    ffmpeg.destroy()
  }
}