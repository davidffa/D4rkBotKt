package me.davidffa.d4rkbotkt.audio.filters.configs

import com.github.natanbc.lavadsp.rotation.RotationPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun eightD(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
    val rotation = RotationPcmAudioFilter(output, format.sampleRate)
    rotation.setRotationSpeed(0.2)

    return rotation
}