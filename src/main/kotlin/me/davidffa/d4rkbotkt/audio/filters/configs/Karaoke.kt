package me.davidffa.d4rkbotkt.audio.filters.configs

import com.github.natanbc.lavadsp.karaoke.KaraokePcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun karaoke(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
    return listOf(KaraokePcmAudioFilter(output, format.channelCount, format.sampleRate).also {
        it.monoLevel = 3.5f
    })
}