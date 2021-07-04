package me.davidffa.d4rkbotkt.audio.filters.configs

import com.github.natanbc.lavadsp.rotation.RotationPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun eightD(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
    return listOf(RotationPcmAudioFilter(output, format.sampleRate).also {
        it.setRotationSpeed(0.2)
    })
}