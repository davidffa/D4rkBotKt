package me.davidffa.d4rkbotkt.audio.filters.configs

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun pop(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
    val bands = FloatArray(Equalizer.BAND_COUNT){ 0f }
    bands[0] = 0.65f
    bands[1] = 0.45f
    bands[2] = -0.45f
    bands[3] = -0.65f
    bands[4] = -0.35f
    bands[5] = 0.45f
    bands[6] = 0.55f
    bands[7] = 0.6f
    bands[8] = 0.6f
    bands[9] = 0.6f

    return listOf(Equalizer(format.channelCount, output, bands))
}