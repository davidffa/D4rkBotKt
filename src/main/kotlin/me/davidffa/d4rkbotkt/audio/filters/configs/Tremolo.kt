package me.davidffa.d4rkbotkt.audio.filters.configs

import com.github.natanbc.lavadsp.tremolo.TremoloPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun tremolo(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
  return listOf(TremoloPcmAudioFilter(output, format.channelCount, format.sampleRate).also {
    it.depth = 0.6f
    it.frequency = 28f
  })
}