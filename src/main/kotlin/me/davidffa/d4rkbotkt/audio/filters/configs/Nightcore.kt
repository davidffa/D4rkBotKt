package me.davidffa.d4rkbotkt.audio.filters.configs

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter
import com.github.natanbc.lavadsp.tremolo.TremoloPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun nightcore(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
    val bands = FloatArray(Equalizer.BAND_COUNT)
    bands[0] = 0.3f
    bands[1] = 0.3f
    val equalizer = Equalizer(format.channelCount, output, bands)

    val timescale = TimescalePcmAudioFilter(equalizer, format.channelCount, format.sampleRate)
    timescale.rate = 1.1
    timescale.pitch = 1.2

    val tremolo = TremoloPcmAudioFilter(timescale, format.channelCount, format.sampleRate)
    tremolo.depth = 0.3f
    tremolo.frequency = 14f

    return listOf(equalizer, timescale, tremolo)
}