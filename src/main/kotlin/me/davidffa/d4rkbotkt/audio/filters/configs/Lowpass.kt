package me.davidffa.d4rkbotkt.audio.filters.configs

import com.github.natanbc.lavadsp.lowpass.LowPassPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun lowpass(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
  return listOf(LowPassPcmAudioFilter(output, format.channelCount, format.sampleRate))
}