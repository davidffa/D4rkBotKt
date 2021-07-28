package me.davidffa.d4rkbotkt.audio.filters.configs

import com.github.natanbc.lavadsp.vibrato.VibratoPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun vibrato(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
  return listOf(VibratoPcmAudioFilter(output, format.channelCount, format.sampleRate).also {
    it.depth = 1.0f
    it.frequency = 14f
  })
}