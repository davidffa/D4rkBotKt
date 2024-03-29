package com.github.davidffa.d4rkbotkt.audio.filters.configs

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun bass(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
  val bands = FloatArray(Equalizer.BAND_COUNT) { 0f }
  bands[0] = 0.29f
  bands[1] = 0.23f
  bands[2] = 0.19f
  bands[3] = 0.16f
  bands[4] = 0.08f

  return listOf(Equalizer(format.channelCount, output, bands))
}