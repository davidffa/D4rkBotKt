package com.github.davidffa.d4rkbotkt.audio.filters

import com.github.natanbc.lavadsp.volume.VolumePcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.github.davidffa.d4rkbotkt.audio.filters.configs.*

class FilterFactory(private val filters: List<Filter>, private val volume: Float = 1f) : PcmFilterFactory {
  override fun buildChain(
    track: AudioTrack?,
    format: AudioDataFormat,
    output: UniversalPcmAudioFilter
  ): MutableList<AudioFilter> {
    val list = mutableListOf<FloatPcmAudioFilter>()

    for (filter in filters) {
      list.addAll(
        when (filter) {
          Filter.BASS -> bass(format, list.lastOrNull() ?: output)
          Filter.DAYCORE -> daycore(format, list.lastOrNull() ?: output)
          Filter.NIGHTCORE -> nightcore(format, list.lastOrNull() ?: output)
          Filter.KARAOKE -> karaoke(format, list.lastOrNull() ?: output)
          Filter.POP -> pop(format, list.lastOrNull() ?: output)
          Filter.SOFT -> soft(format, list.lastOrNull() ?: output)
          Filter.TREBLEBASS -> treblebass(format, list.lastOrNull() ?: output)
          Filter.VAPORWAVE -> vaporwave(format, list.lastOrNull() ?: output)
          Filter.VIBRATO -> vibrato(format, list.lastOrNull() ?: output)
          Filter.VOCALS -> vocals(format, list.lastOrNull() ?: output)
          Filter.EIGHTD -> eightD(format, list.lastOrNull() ?: output)
          Filter.DISTORTION -> distortion(format, list.lastOrNull() ?: output)
          Filter.LOWPASS -> lowpass(format, list.lastOrNull() ?: output)
          Filter.TREMOLO -> tremolo(format, list.lastOrNull() ?: output)
        }
      )
    }

    if (volume != 1f) {
      list.add(VolumePcmAudioFilter(list.lastOrNull() ?: output).also {
        it.volume = volume
      })
    }

    return list.reversed().toMutableList()
  }
}