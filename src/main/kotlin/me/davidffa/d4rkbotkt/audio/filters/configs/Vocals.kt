package me.davidffa.d4rkbotkt.audio.filters.configs

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun vocals(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
  val bands = FloatArray(Equalizer.BAND_COUNT) { 0f }
  bands[0] = -0.25f
  bands[1] = -0.25f
  bands[2] = -0.25f
  bands[3] = -0.25f
  bands[4] = 0.15f
  bands[5] = 0.25f
  bands[7] = 0.25f
  bands[8] = 0.2f
  bands[9] = 0.15f
  bands[10] = 0.1f
  bands[11] = 0.05f
  bands[12] = -0.25f
  bands[13] = -0.25f
  bands[14] = -0.25f

  return listOf(Equalizer(format.channelCount, output, bands))
}