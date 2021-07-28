package me.davidffa.d4rkbotkt.audio.filters.configs

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter
import com.github.natanbc.lavadsp.tremolo.TremoloPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun daycore(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
  val timescale = TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate).also {
    it.rate = 0.8
    it.pitch = 0.7
  }

  val tremolo = TremoloPcmAudioFilter(timescale, format.channelCount, format.sampleRate).also {
    it.depth = 0.3f
    it.frequency = 14f
  }

  return listOf(timescale, tremolo)
}