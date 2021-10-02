package me.davidffa.d4rkbotkt.audio.receive

import com.cloudburst.lame.lowlevel.LameEncoder
import com.cloudburst.lame.mp3.Lame
import com.cloudburst.lame.mp3.MPEGMode
import net.dv8tion.jda.api.audio.AudioReceiveHandler
import net.dv8tion.jda.api.audio.CombinedAudio
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths

class AudioReceiver(
  guildId: String
) : AudioReceiveHandler {
  private val volume = 1.0

  private val encoder = LameEncoder(
    AudioReceiveHandler.OUTPUT_FORMAT,
    96,
    MPEGMode.STEREO,
    Lame.QUALITY_MIDDLE_LOW,
    false
  )

  private val stream: OutputStream

  override fun canReceiveEncoded() = false
  override fun canReceiveUser() = false
  override fun canReceiveCombined() = true

  init {
    Files.createDirectories(Paths.get("./records"))

    stream = FileOutputStream("./records/record-${guildId}.mp3")
  }

  override fun handleCombinedAudio(combinedAudio: CombinedAudio) {
    val inputBuffer = combinedAudio.getAudioData(volume)
    val outputBuffer = ByteArray(inputBuffer.size)

    val bytesWritten = encoder.encodeBuffer(inputBuffer, 0, inputBuffer.size, outputBuffer)
    stream.write(outputBuffer, 0, bytesWritten)
  }

  fun close() {
    stream.close()
    encoder.close()
  }
}