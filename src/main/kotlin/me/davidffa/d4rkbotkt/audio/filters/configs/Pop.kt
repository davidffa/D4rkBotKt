package me.davidffa.d4rkbotkt.audio.filters.configs

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun pop(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
  val bands = FloatArray(Equalizer.BAND_COUNT) { 0f }
  bands[0] = -0.09f
  bands[1] = -0.09f
  bands[2] = -0.06f
  bands[3] = 0.02f
  bands[4] = 0.04f
  bands[5] = 0.16f
  bands[6] = 0.18f
  bands[7] = 0.22f
  bands[8] = 0.22f
  bands[9] = 0.18f
  bands[10] = 0.12f
  bands[11] = 0.02f
  bands[12] = -0.03f
  bands[13] = -0.06f
  bands[14] = -0.1f

  return listOf(Equalizer(format.channelCount, output, bands))
}