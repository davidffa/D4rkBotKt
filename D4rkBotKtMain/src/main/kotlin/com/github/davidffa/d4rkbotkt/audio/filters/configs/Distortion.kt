package com.github.davidffa.d4rkbotkt.audio.filters.configs

import com.github.natanbc.lavadsp.distortion.DistortionConverter
import com.github.natanbc.lavadsp.distortion.DistortionPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun distortion(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
  return listOf(DistortionPcmAudioFilter(output, format.channelCount).also {
    it.tanOffset = 0.5f
    it.tanScale = 0.8f

    it.enableFunctions(DistortionConverter.TAN)
  })
}