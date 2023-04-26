package com.github.davidffa.d4rkbotkt.audio.filters.configs

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun soft(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
  val bands = FloatArray(Equalizer.BAND_COUNT) { 0f }
  bands[8] = -0.25f
  bands[9] = -0.25f
  bands[10] = -0.25f
  bands[11] = -0.25f
  bands[12] = -0.25f
  bands[13] = -0.25f

  return listOf(Equalizer(format.channelCount, output, bands))
}