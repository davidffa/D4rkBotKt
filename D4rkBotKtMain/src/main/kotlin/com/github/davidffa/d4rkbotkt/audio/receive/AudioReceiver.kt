package com.github.davidffa.d4rkbotkt.audio.receive

import com.github.davidffa.d4rkbotkt.natives.mp3.Mp3Encoder
import net.dv8tion.jda.api.audio.AudioReceiveHandler
import net.dv8tion.jda.api.audio.CombinedAudio
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.math.min

class AudioReceiver(
  guildId: String,
  vcBitrate: Int
) : AudioReceiveHandler {
  companion object {
    private const val FRAME_SIZE = 960
    private const val SAMPLE_RATE = 48000
    private const val VOLUME = 1.0

    // lame.h#L701
    fun calcSafeFrameSize(bitrate: Int, sampleRate: Int)
            = FRAME_SIZE * (bitrate / 8) / sampleRate + 4 * 1152 * (bitrate / 8) / sampleRate + 512
  }

  private val outputChannel: FileChannel

  private val encoder = Mp3Encoder(SAMPLE_RATE, 2, min(vcBitrate, 180000))

  // 1 frame -> FRAME_SIZE * channels * 2 (16 bit pcm)
  private val inputBuf = ByteBuffer.allocateDirect(FRAME_SIZE * 2 * 2)
          .order(ByteOrder.nativeOrder())
  private val tempBuf = ByteBuffer.allocateDirect(calcSafeFrameSize(min(vcBitrate, 180000), SAMPLE_RATE))
          .order(ByteOrder.nativeOrder())

  init {
    Files.createDirectories(Path("./records"))

    outputChannel = FileChannel.open(
            Path("./records/record-${guildId}.mp3"),
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
    )
  }

  override fun canReceiveEncoded() = false
  override fun canReceiveUser() = false
  override fun canReceiveCombined() = true

  init {
    Files.createDirectories(Paths.get("./records"))

    // Audio format: AudioSendHandler.INPUT_FORMAT (48k 16 bit big endian pcm)
    //    val args = arrayOf("-f", "s16be", "-ar", "48k", "-ac", "2", "-i", "pipe:0", "-b:a", min(bitrate, 128000).toString(), "-f", "mp3", "pipe:1")
    //
    //    ffmpeg = ProcessBuilder("ffmpeg", *args)
    //      .redirectError(ProcessBuilder.Redirect.DISCARD)  // Logs
    //      .redirectInput(ProcessBuilder.Redirect.PIPE)
    //      .redirectOutput(File("./records/record-${guildId}.mp3"))
    //      .start()
  }

  override fun handleCombinedAudio(combinedAudio: CombinedAudio) {
    val audio = combinedAudio.getAudioData(VOLUME)

    for (i in 0 until audio.size - 1) {
      val tmp = audio[i]
      audio[i] = audio[i+1]
      audio[i+1] = tmp
    }

    inputBuf.put(audio).flip()
    encoder.encodeStereo(inputBuf.asShortBuffer(), FRAME_SIZE, tempBuf)

    outputChannel.write(tempBuf)

    // ffmpeg.outputStream.write(combinedAudio.getAudioData(volume))
  }

  fun close() {
    // 7200 bytes recommended on lame.h#L868
    val finalMp3Frames = ByteBuffer.allocateDirect(7200)
            .order(ByteOrder.nativeOrder())

    encoder.flush(finalMp3Frames)
    encoder.close()

    outputChannel.write(finalMp3Frames)
    outputChannel.close()
  }
}